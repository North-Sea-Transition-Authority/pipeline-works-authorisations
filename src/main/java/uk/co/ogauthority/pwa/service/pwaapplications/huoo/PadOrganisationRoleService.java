package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.huoo.AddHuooController;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.HuooOrganisationUnitRoleView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.HuooTreatyAgreementView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.validators.huoo.HuooValidationView;

@Service
public class PadOrganisationRoleService implements ApplicationFormSectionService {

  private final PadOrganisationRolesRepository padOrganisationRolesRepository;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public PadOrganisationRoleService(
      PadOrganisationRolesRepository padOrganisationRolesRepository,
      PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.padOrganisationRolesRepository = padOrganisationRolesRepository;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  public List<PadOrganisationRole> getOrgRolesForDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padOrganisationRolesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  public PadOrganisationRole getOrganisationRole(PwaApplicationDetail pwaApplicationDetail, Integer id) {
    return padOrganisationRolesRepository.getByPwaApplicationDetailAndId(pwaApplicationDetail, id)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to find org role with ID: " + id));
  }

  public List<HuooOrganisationUnitRoleView> getHuooOrganisationUnitRoleViews(PwaApplicationDetail detail,
                                                                             List<PadOrganisationRole> padOrganisationRoleList) {

    // filter so we are only looking at portal organisation roles
    Map<PortalOrganisationUnit, List<PadOrganisationRole>> orgRoles = padOrganisationRoleList.stream()
        .filter(orgRole -> orgRole.getType().equals(HuooType.PORTAL_ORG))
        .collect(Collectors.groupingBy(PadOrganisationRole::getOrganisationUnit));

    // get the org units so that we can query the details for each
    var portalOrgUnits = new ArrayList<>(orgRoles.keySet());

    Map<Integer, PortalOrganisationUnitDetail> portalOrgUnitDetails = portalOrganisationsAccessor
        .getOrganisationUnitDetails(portalOrgUnits).stream()
        .collect(Collectors.toMap(PortalOrganisationUnitDetail::getOuId, orgUnitDetail -> orgUnitDetail));

    return orgRoles.keySet()
        .stream()
        .map(orgUnit -> {

          PortalOrganisationUnitDetail orgUnitDetail = portalOrgUnitDetails.getOrDefault(
              orgUnit.getOuId(), null);

          boolean canRemoveOrg = canRemoveOrgRoleFromUnit(detail, orgUnit);

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
        .collect(Collectors.toList());

  }

  private String getEditHuooUrl(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {
    return ReverseRouter.route(on(AddHuooController.class)
        .renderEditOrgHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationUnit.getOuId(), null, null, null));
  }

  private String getEditHuooUrl(PwaApplicationDetail detail, PadOrganisationRole organisationRole) {
    return ReverseRouter.route(on(AddHuooController.class)
        .renderEditTreatyHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationRole.getId(), null, null, null));
  }

  private String getRemoveHuooUrl(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {
    return ReverseRouter.route(on(AddHuooController.class)
        .postDeleteOrgHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationUnit.getOuId(), null, null, null, null));
  }

  private String getRemoveHuooUrl(PwaApplicationDetail detail, PadOrganisationRole organisationRole) {
    return ReverseRouter.route(on(AddHuooController.class)
        .postDeleteTreatyHuoo(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            organisationRole.getId(), null, null, null, null));
  }

  public List<HuooTreatyAgreementView> getTreatyAgreementViews(PwaApplicationDetail detail,
                                                               List<PadOrganisationRole> padOrganisationRoleList) {
    return padOrganisationRoleList.stream()
        .filter(padOrganisationRole -> padOrganisationRole.getType().equals(HuooType.TREATY_AGREEMENT))
        .map(treatyRole -> new HuooTreatyAgreementView(
            treatyRole,
            getEditHuooUrl(detail, treatyRole),
            getRemoveHuooUrl(detail, treatyRole)))
        .sorted(Comparator.comparing(HuooTreatyAgreementView::getCountry))
        .collect(Collectors.toList());
  }

  /**
   * If the organisation being removed is a holder, return true if there is > 1 holder on the application, false otherwise.
   * If the organisation being removed isn't a holder, return true.
   */
  public boolean canRemoveOrgRoleFromUnit(PwaApplicationDetail detail, PortalOrganisationUnit orgUnit) {
    var units = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail, orgUnit);
    var roles = units.stream()
        .map(PadOrganisationRole::getRole)
        .collect(Collectors.toSet());

    var countMap = getRoleCountMap(detail);

    if (roles.contains(HuooRole.HOLDER)) {
      return countMap.get(HuooRole.HOLDER) > 1;
    }
    return true;
  }

  @Transactional
  public void removeRolesOfUnit(PwaApplicationDetail pwaApplicationDetail, PortalOrganisationUnit organisationUnit) {
    var roles = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(pwaApplicationDetail,
        organisationUnit);
    padOrganisationRolesRepository.deleteAll(roles);
  }

  @Transactional
  public void removeRoleOfTreatyAgreement(PadOrganisationRole organisationRole) {
    padOrganisationRolesRepository.delete(organisationRole);
  }

  public void mapPortalOrgUnitRoleToForm(PwaApplicationDetail detail, PortalOrganisationUnit orgUnit, HuooForm form) {
    var roles = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail, orgUnit);
    var roleSet = roles.stream()
        .map(PadOrganisationRole::getRole)
        .collect(Collectors.toSet());

    var role = roles.stream()
        .findFirst()
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "No organisation unit roles found for org unit with ID: " + orgUnit.getOuId()));
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setHuooRoles(roleSet);
    form.setOrganisationUnit(role.getOrganisationUnit());
  }

  public void mapTreatyAgreementToForm(PwaApplicationDetail pwaApplicationDetail, PadOrganisationRole organisationRole,
                                       HuooForm form) {
    if (organisationRole.getAgreement() == null) {
      throw new ActionNotAllowedException(
          "Attempting to edit a non-treaty agreement org with ID: " + organisationRole.getId());
    }
    form.setHuooType(organisationRole.getType());
    form.setHuooRoles(Set.of(organisationRole.getRole()));
    form.setTreatyAgreement(organisationRole.getAgreement());
  }

  /**
   * Removes existing linked entries of the organisationUnit, and creates the entries from the form information.
   *
   * @param pwaApplicationDetail The application detail
   * @param form                 A validated HuooForm.
   */
  @Transactional
  public void saveEntityUsingForm(PwaApplicationDetail pwaApplicationDetail, HuooForm form) {
    var rolesToSave = new ArrayList<PadOrganisationRole>();
    if (form.getHuooType().equals(HuooType.PORTAL_ORG)) {
      form.getHuooRoles().forEach(huooRole -> {
        var padOrganisationRole = new PadOrganisationRole();
        padOrganisationRole.setAgreement(null);
        padOrganisationRole.setPwaApplicationDetail(pwaApplicationDetail);
        padOrganisationRole.setRole(huooRole);
        padOrganisationRole.setType(form.getHuooType());
        padOrganisationRole.setOrganisationUnit(form.getOrganisationUnit());
        rolesToSave.add(padOrganisationRole);
      });
    } else if (form.getHuooType().equals(HuooType.TREATY_AGREEMENT)) {
      var padOrganisationRole = new PadOrganisationRole();
      padOrganisationRole.setAgreement(form.getTreatyAgreement());
      padOrganisationRole.setPwaApplicationDetail(pwaApplicationDetail);
      padOrganisationRole.setOrganisationUnit(null);
      padOrganisationRole.setRole(HuooRole.USER);
      padOrganisationRole.setType(form.getHuooType());
      rolesToSave.add(padOrganisationRole);
    }
    padOrganisationRolesRepository.saveAll(rolesToSave);
  }

  @Transactional
  public void updateEntityUsingForm(PwaApplicationDetail pwaApplicationDetail, PortalOrganisationUnit organisationUnit,
                                    HuooForm form) {
    var roles = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(pwaApplicationDetail,
        organisationUnit);
    padOrganisationRolesRepository.deleteAll(roles);
    saveEntityUsingForm(pwaApplicationDetail, form);
  }

  @Transactional
  public void updateEntityUsingForm(PwaApplicationDetail pwaApplicationDetail, PadOrganisationRole organisationRole,
                                    HuooForm form) {
    organisationRole.setAgreement(form.getTreatyAgreement());
    padOrganisationRolesRepository.save(organisationRole);
  }

  @Transactional
  public void addHolder(PwaApplicationDetail detail, PortalOrganisationUnit organisationUnit) {

    var holderRole = new PadOrganisationRole();
    holderRole.setPwaApplicationDetail(detail);
    holderRole.setType(HuooType.PORTAL_ORG);
    holderRole.setRole(HuooRole.HOLDER);
    holderRole.setOrganisationUnit(organisationUnit);
    padOrganisationRolesRepository.save(holderRole);

  }

  public HuooValidationView getValidationViewForOrg(PwaApplicationDetail pwaApplicationDetail,
                                                    PortalOrganisationUnit portalOrganisationUnit) {
    var roles = padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(pwaApplicationDetail,
        portalOrganisationUnit);
    return new HuooValidationView(new HashSet<>(roles));
  }

  public HuooValidationView getValidationViewForTreaty(PwaApplicationDetail pwaApplicationDetail,
                                                       PadOrganisationRole padOrganisationRole) {
    return new HuooValidationView(Set.of(padOrganisationRole));
  }

  /**
   * Return a count of all organisation roles currently on the application.
   * @param pwaApplicationDetail The application detail.
   * @return A map with the role as key, and count as value.
   */
  @VisibleForTesting
  public Map<HuooRole, Integer> getRoleCountMap(PwaApplicationDetail pwaApplicationDetail) {
    var padOrganisationRoleList = getOrgRolesForDetail(pwaApplicationDetail);

    var map = new HashMap<HuooRole, Integer>();
    HuooRole.stream()
        .forEach(role -> map.put(role, 0));

    padOrganisationRoleList.stream()
        .map(PadOrganisationRole::getRole)
        .forEach(role -> map.put(role, map.get(role) + 1));

    return map;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var roleCountMap = getRoleCountMap(detail);
    return roleCountMap.get(HuooRole.HOLDER) > 0
        && roleCountMap.get(HuooRole.USER) > 0
        && roleCountMap.get(HuooRole.OPERATOR) > 0
        && roleCountMap.get(HuooRole.OWNER) > 0;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    if (validationType == ValidationType.FULL) {
      var roleCountMap = getRoleCountMap(pwaApplicationDetail);
      if (roleCountMap.get(HuooRole.HOLDER) == 0) {
        bindingResult.reject("holders" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "At least one holder is required");
      }
      if (roleCountMap.get(HuooRole.USER) == 0) {
        bindingResult.reject("users" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "At least one user is required");
      }
      if (roleCountMap.get(HuooRole.OPERATOR) == 0) {
        bindingResult.reject("operators" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "At least one operator is required");
      }
      if (roleCountMap.get(HuooRole.OWNER) == 0) {
        bindingResult.reject("owners" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "At least one owner is required");
      }
    }
    return bindingResult;
  }
}
