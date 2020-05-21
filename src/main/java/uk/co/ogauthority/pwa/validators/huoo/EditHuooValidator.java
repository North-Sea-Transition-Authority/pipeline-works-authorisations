package uk.co.ogauthority.pwa.validators.huoo;

import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;

@Service
public class EditHuooValidator implements SmartValidator {

  private final PadOrganisationRoleService padOrganisationRoleService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public EditHuooValidator(
      PadOrganisationRoleService padOrganisationRoleService,
      PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(HuooForm.class);
  }

  @Override
  @Deprecated
  public void validate(Object target, Errors errors) {
    throw new AssertionError(); /* required by the SmartValidator. Not actually used. */
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (HuooForm) target;
    var detail = (PwaApplicationDetail) validationHints[0];
    var huooValidationView = (HuooValidationView) validationHints[1];
    var roles = padOrganisationRoleService.getOrgRolesForDetail(detail);
    if (form.getHuooType() == null) {
      errors.rejectValue("huooType", "huooType.required",
          "You must select the entity type");
    }

    if (form.getHuooType() == HuooType.PORTAL_ORG) {
      portalOrganisationsAccessor.getOrganisationUnitById(form.getOrganisationUnitId())
          .orElseGet(() -> {
            errors.rejectValue("organisationUnitId",
                "organisationUnitId" + FieldValidationErrorCodes.INVALID.getCode(),
                "The selected organisation is invalid");
            return null;
          });
      if (SetUtils.emptyIfNull(form.getHuooRoles()).isEmpty()) {
        errors.rejectValue("huooRoles", "huooRoles.required",
            "You must select one or more roles");
      }
      if (form.getOrganisationUnitId() != null) {
        roles.stream()
            .filter(role -> role.getType().equals(HuooType.PORTAL_ORG))
            .filter(
                // we aren't editing an org at all, but a treaty
                padOrganisationRole -> huooValidationView.getPortalOrganisationUnit() == null
                    || (padOrganisationRole.getOrganisationUnit().getOuId() != huooValidationView.getPortalOrganisationUnit().getOuId()))
            .filter(padOrganisationRole ->
                form.getOrganisationUnitId().equals(padOrganisationRole.getOrganisationUnit().getOuId()))
            .findAny()
            .ifPresent(padOrganisationRole -> errors.rejectValue("organisationUnit", "organisationUnit.alreadyUsed",
                "The selected organisation is already added to the application"));
      } else {
        errors.rejectValue("organisationUnit", "organisationUnit.required",
            "You must select an organisation");
      }
    } else if (form.getHuooType() == HuooType.TREATY_AGREEMENT && form.getTreatyAgreement() == null) {
      errors.rejectValue("treatyAgreement", "treatyAgreement.required",
          "You must select a treaty agreement");
    }

    var holderCount = roles.stream()
        .filter(padOrgRole -> padOrgRole.getType().equals(huooValidationView.getHuooType()))
        .filter(padOrgRole -> padOrgRole.getType().equals(HuooType.PORTAL_ORG))
        .filter(padOrgRole -> padOrgRole.getRole().equals(HuooRole.HOLDER))
        .filter(
            padOrgRole -> padOrgRole.getOrganisationUnit().getOuId() != huooValidationView.getPortalOrganisationUnit().getOuId())
        .count();
    // TODO: PWA-386 Change hard-coded 1 to match number of potential holders on an application.
    if (holderCount >= 1) {
      if (SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER)) {
        errors.rejectValue("huooRoles", "huooRoles.alreadyUsed",
            "You may only have one holder on an application");
      }
    }

    if (huooValidationView.getHuooType() != form.getHuooType()) {
      errors.rejectValue("huooType", "huooType.differentType",
          "Entity cannot have a different type");
    }

    var orgWasHolder = roles.stream()
        .filter(padOrgRole -> padOrgRole.getType().equals(huooValidationView.getHuooType()))
        .filter(padOrgRole -> padOrgRole.getType().equals(HuooType.PORTAL_ORG))
        .filter(
            padOrgRole -> padOrgRole.getOrganisationUnit().getOuId() == huooValidationView.getPortalOrganisationUnit().getOuId())
        .anyMatch(padOrgRole -> padOrgRole.getRole().equals(HuooRole.HOLDER));

    if (orgWasHolder) {
      if (!SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER) && holderCount == 0) {
        errors.rejectValue("huooRoles", "huooRoles.requiresOneHolder",
            "You can't remove the final holder on an application");
      }
    }

    if (form.getHuooType() == HuooType.TREATY_AGREEMENT) {
      var alreadyAddedTreaty = roles.stream()
          .filter(padOrganisationRole -> padOrganisationRole.getType().equals(HuooType.TREATY_AGREEMENT))
          .filter(padOrganisationRole -> padOrganisationRole.getAgreement() != huooValidationView.getTreatyAgreement())
          .anyMatch(padOrganisationRole -> padOrganisationRole.getAgreement().equals(form.getTreatyAgreement()));
      if (alreadyAddedTreaty) {
        errors.rejectValue("treatyAgreement", "treatyAgreement.duplicate",
            "The treaty agreement is already added to the application");
      }
    }
  }
}
