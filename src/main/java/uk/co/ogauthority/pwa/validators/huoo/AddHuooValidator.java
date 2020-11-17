package uk.co.ogauthority.pwa.validators.huoo;

import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
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
    return clazz.equals(HuooForm.class);
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
    if (form.getHuooType() == null) {
      errors.rejectValue("huooType", "huooType.required",
          "Select the entity type");
    } else if (form.getHuooType() == HuooType.PORTAL_ORG) {
      if (SetUtils.emptyIfNull(form.getHuooRoles()).isEmpty()) {
        errors.rejectValue("huooRoles", "huooRoles.required",
            "Select one or more roles");
      }
      if (form.getOrganisationUnitId() != null) {
        roles.stream()
            .filter(role -> role.getType().equals(HuooType.PORTAL_ORG))
            .filter(padOrganisationRole ->
                form.getOrganisationUnitId().equals(padOrganisationRole.getOrganisationUnit().getOuId()))
            .findAny()
            .ifPresent(padOrganisationRole -> errors.rejectValue("organisationUnitId", "organisationUnitId.alreadyUsed",
                "The selected organisation is already added to the application"));
      } else {
        errors.rejectValue("organisationUnitId", "organisationUnitId.required",
            "Select an organisation");
      }
    }

    var holderCount = roles.stream()
        .filter(padOrgRole -> padOrgRole.getRole().equals(HuooRole.HOLDER))
        .count();
    if (holderCount >= detail.getNumOfHolders()) {
      if (SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER)) {
        var holdersTxt = detail.getNumOfHolders() > 1 ? "holders" : "holder";
        errors.rejectValue("huooRoles", "huooRoles.holderNotAllowed",
            "You may only have " + detail.getNumOfHolders() + " " + holdersTxt +  " on an application");
      }
    }
    if (form.getHuooType() == HuooType.TREATY_AGREEMENT && SetUtils.emptyIfNull(form.getHuooRoles()).contains(HuooRole.HOLDER)) {
      errors.rejectValue("huooRoles", "huooRoles.treatyHolderNotAllowed",
          "A treaty agreement cannot be an application holder");
    }

    if (form.getHuooType() == HuooType.TREATY_AGREEMENT) {
      var treatyCount = roles.stream()
          .filter(padOrganisationRole -> padOrganisationRole.getType().equals(HuooType.TREATY_AGREEMENT))
          .count();
      if (treatyCount > 0) {
        errors.rejectValue("huooType", "huooType" + FieldValidationErrorCodes.TOO_MANY.getCode(),
            "You may only have one treaty agreement on an application");
      }
    }
  }




}
