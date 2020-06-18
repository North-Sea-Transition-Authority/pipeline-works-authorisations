package uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.EditBlockCrossingForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Component
public class EditBlockCrossingFormValidator implements SmartValidator {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public EditBlockCrossingFormValidator(PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return EditBlockCrossingForm.class.isAssignableFrom(clazz);
  }

  @Override
  @Deprecated
  public void validate(Object target, Errors errors) {

  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (EditBlockCrossingForm) target;
    var licence = (PearsLicence) validationHints[0];

    if (form.getCrossedBlockOwner() != null
        && form.getCrossedBlockOwner().equals(CrossedBlockOwner.PORTAL_ORGANISATION)) {
      ValidationUtils.rejectIfEmpty(
          errors, "blockOwnersOuIdList",
          "blockOwnersOuIdList.required",
          "Select a block owner"
      );
    }

    if (form.getCrossedBlockOwner() != null && form.getCrossedBlockOwner() != CrossedBlockOwner.UNLICENCED) {
      if (licence == null) {
        errors.rejectValue("crossedBlockOwner", "crossedBlockOwner" + FieldValidationErrorCodes.INVALID.getCode(),
            "Unlicensed blocks cannot have an owner");
      }
    }

    if (form.getCrossedBlockOwner() == CrossedBlockOwner.UNLICENCED) {
      if (licence != null) {
        errors.rejectValue("crossedBlockOwner", "crossedBlockOwner" + FieldValidationErrorCodes.INVALID.getCode(),
            "Licensed blocks must have an owner");
      }
    }

    if (form.getBlockOwnersOuIdList() != null && !form.getBlockOwnersOuIdList().isEmpty()) {
      var validOrganisationUnitIdSet = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(
          form.getBlockOwnersOuIdList())
          .stream()
          .map(PortalOrganisationUnit::getOuId)
          .collect(Collectors.toSet());

      for (int i = 0; i < form.getBlockOwnersOuIdList().size(); i++) {
        if (!validOrganisationUnitIdSet.contains(form.getBlockOwnersOuIdList().get(i))) {
          var field = "blockOwnersOuIdList[" + i + "]";
          errors.rejectValue(field, field + ".invalid", "Select a valid organisation unit");
        }
      }

    }
  }
}
