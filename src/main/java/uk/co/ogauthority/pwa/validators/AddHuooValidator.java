package uk.co.ogauthority.pwa.validators;

import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;

@Service
public class AddHuooValidator implements SmartValidator {

  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public AddHuooValidator(
      PadOrganisationRoleService padOrganisationRoleService) {
    this.padOrganisationRoleService = padOrganisationRoleService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(AddHuooValidator.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new AssertionError(); /* required by the SmartValidator. Not actually used. */
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (HuooForm) target;
    var detail = (PwaApplicationDetail) validationHints[0];
    var roles = padOrganisationRoleService.getOrgRolesForDetail(detail);
    if (SetUtils.emptyIfNull(form.getHuooRoles()).isEmpty()) {
      errors.rejectValue("huooRoles", "huooRoles.required",
          "You must select one or more roles");
    }
    if (form.getHuooType() == null) {
      errors.rejectValue("huooType", "huooType.required",
          "You must select the entity type");
    }
    if (form.getHuooType() == HuooType.PORTAL_ORG) {
      if (form.getOrganisationUnit() != null) {
        roles.stream()
            .filter(role -> role.getType().equals(HuooType.PORTAL_ORG))
            .filter(padOrganisationRole ->
                padOrganisationRole.getOrganisationUnit().getOuId() == form.getOrganisationUnit().getOuId())
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
        .filter(padOrgRole -> padOrgRole.getRoles().contains(HuooRole.HOLDER))
        .count();
    // TODO: PWA-407 Change hard-coded 1 to match number of potential holders on an application.
    if (holderCount >= 1) {
      if (SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER)) {
        errors.rejectValue("huooRoles", "huooRoles.holderNotAllowed",
            "You may only have one holder on an application");
      }
    }
    if (form.getHuooType() == HuooType.TREATY_AGREEMENT && SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER)) {
      errors.rejectValue("huooRoles", "huooRoles.treatyHolderNotAllowed",
          "A treaty agreement cannot be an application holder");
    }

    if (form.getHuooType() == HuooType.TREATY_AGREEMENT) {
      var alreadyAddedTreaty = roles.stream()
          .filter(padOrganisationRole -> padOrganisationRole.getType().equals(HuooType.TREATY_AGREEMENT))
          .anyMatch(padOrganisationRole -> padOrganisationRole.getAgreement().equals(form.getTreatyAgreement()));
      if (alreadyAddedTreaty) {
        errors.rejectValue("treatyAgreement", "treatyAgreement.duplicate",
            "The treaty agreement is already added to the application");
      }
    }
  }
}
