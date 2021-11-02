package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.controller.AddHuooController;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Helper methods and view object construction logic to directly support the HUOO summary application screen.
 */
@Service
public class PadHuooSummaryViewService {

  private final PadOrganisationRoleService padOrganisationRoleService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public PadHuooSummaryViewService(PadOrganisationRoleService padOrganisationRoleService,
                                   PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  public PadHuooSummaryView getPadHuooSummaryView(PwaApplicationDetail detail) {

    var allOrgRoles = padOrganisationRoleService.getOrgRolesForDetail(detail);

    return new PadHuooSummaryView(
        getHuooOrganisationUnitRoleViews(detail, allOrgRoles),
        getTreatyAgreementViews(detail, allOrgRoles),
        canShowHolderGuidance(detail)
    );

  }

  @VisibleForTesting
  boolean canShowHolderGuidance(PwaApplicationDetail pwaApplicationDetail) {
    return pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.INITIAL);
  }

  @VisibleForTesting
  List<HuooOrganisationUnitRoleView> getHuooOrganisationUnitRoleViews(PwaApplicationDetail detail,
                                                                             List<PadOrganisationRole> padOrganisationRoleList) {

    // filter so we are only looking at portal organisation roles
    Map<PortalOrganisationUnit, List<PadOrganisationRole>> orgRoles = padOrganisationRoleList.stream()
        .filter(orgRole -> orgRole.getType().equals(HuooType.PORTAL_ORG))
        .collect(Collectors.groupingBy(PadOrganisationRole::getOrganisationUnit));

    // get the org units so that we can query the details for each
    var portalOrgUnits = new ArrayList<>(orgRoles.keySet());

    Map<Integer, PortalOrganisationUnitDetail> portalOrgUnitDetails = portalOrganisationsAccessor
        .getOrganisationUnitDetails(portalOrgUnits).stream()
        .collect(toMap(PortalOrganisationUnitDetail::getOuId, orgUnitDetail -> orgUnitDetail));

    return orgRoles.keySet()
        .stream()
        .map(orgUnit -> {

          PortalOrganisationUnitDetail orgUnitDetail = portalOrgUnitDetails.getOrDefault(
              orgUnit.getOuId(), null);

          boolean canRemoveOrg = padOrganisationRoleService.canRemoveOrgRoleFromUnit(detail, orgUnit);

          var roles = orgRoles.get(orgUnit)
              .stream()
              .map(PadOrganisationRole::getRole)
              .collect(Collectors.toSet());

          return new HuooOrganisationUnitRoleView(
              orgUnitDetail,
              roles,
              getEditHuooUrl(detail, orgUnit),
              canRemoveOrg ? getRemoveHuooUrl(detail, orgUnit) : null);

        })
        .sorted()
        .collect(toList());

  }

  @VisibleForTesting
  List<HuooTreatyAgreementView> getTreatyAgreementViews(PwaApplicationDetail detail,
                                                               List<PadOrganisationRole> padOrganisationRoleList) {
    return padOrganisationRoleList.stream()
        .filter(padOrganisationRole -> padOrganisationRole.getType().equals(HuooType.TREATY_AGREEMENT))
        .map(treatyRole -> new HuooTreatyAgreementView(
            treatyRole,
            getRemoveHuooUrl(detail, treatyRole)))
        .sorted(Comparator.comparing(HuooTreatyAgreementView::getRoles))
        .collect(toList());
  }

  private String getEditHuooUrl(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {
    return ReverseRouter.route(on(AddHuooController.class)
        .renderEditOrgHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationUnit.getOuId(), null, null, null));
  }

  private String getRemoveHuooUrl(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {
    return ReverseRouter.route(on(AddHuooController.class)
        .renderRemoveOrgHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationUnit.getOuId(), null));
  }

  private String getRemoveHuooUrl(PwaApplicationDetail detail, PadOrganisationRole organisationRole) {
    return ReverseRouter.route(on(AddHuooController.class)
        .renderRemoveTreatyHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationRole.getId(), null));
  }


}
