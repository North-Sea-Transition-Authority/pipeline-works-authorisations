package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;

/**
 * Provides validation methods and helper methods for the HUOO summary page.
 */
@Service
public class PadHuooValidationService {

  private final PadOrganisationRolesRepository padOrganisationRolesRepository;
  private final PadHuooRoleMetadataProvider padHuooRoleMetadataProvider;

  @Autowired
  public PadHuooValidationService(PadOrganisationRolesRepository padOrganisationRolesRepository,
                                  PadHuooRoleMetadataProvider padHuooRoleMetadataProvider) {
    this.padOrganisationRolesRepository = padOrganisationRolesRepository;
    this.padHuooRoleMetadataProvider = padHuooRoleMetadataProvider;
  }


  /**
   * Produce a validation result of the HUOO state of a particular application detail.
   */
  public HuooSummaryValidationResult getHuooSummaryValidationResult(PwaApplicationDetail pwaApplicationDetail) {

    var unassignedRoles = padHuooRoleMetadataProvider.getRoleCountMap(pwaApplicationDetail).entrySet()
        .stream()
        .filter(entry -> entry.getValue() == 0)
        .map(Map.Entry::getKey)
        .collect(Collectors.toUnmodifiableSet());

    var inactiveOrgUnitNameList = getInactiveOrganisationNamesWithRole(pwaApplicationDetail);

    var breachedBusinessRules = EnumSet.noneOf(HuooSummaryValidationResult.HuooRules.class);
    if (!doesApplicationHaveValidUsers(pwaApplicationDetail)) {
      breachedBusinessRules.add(HuooSummaryValidationResult.HuooRules.CANNOT_HAVE_TREATY_AND_PORTAL_ORG_USERS);
    }

    return new HuooSummaryValidationResult(unassignedRoles, inactiveOrgUnitNameList, breachedBusinessRules);

  }



  @VisibleForTesting
  List<String> getInactiveOrganisationNamesWithRole(PwaApplicationDetail pwaApplicationDetail) {
    return padOrganisationRolesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .filter(padOrganisationRole -> padOrganisationRole.getType() == HuooType.PORTAL_ORG)
        .map(PadOrganisationRole::getOrganisationUnit)
        .filter(portalOrganisationUnit -> !portalOrganisationUnit.isActive())
        .map(PortalOrganisationUnit::getName)
        .distinct()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toUnmodifiableList());

  }


  @VisibleForTesting
  boolean doesApplicationHaveValidUsers(PwaApplicationDetail pwaApplicationDetail) {

    var totalUserPortalOrgsOnApp = padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        pwaApplicationDetail, HuooRole.USER, HuooType.PORTAL_ORG);

    var totalUserTreatiesOnApp = padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        pwaApplicationDetail, HuooRole.USER, HuooType.TREATY_AGREEMENT);

    return totalUserPortalOrgsOnApp > 0 && totalUserTreatiesOnApp > 0 ? false : true;


  }


}
