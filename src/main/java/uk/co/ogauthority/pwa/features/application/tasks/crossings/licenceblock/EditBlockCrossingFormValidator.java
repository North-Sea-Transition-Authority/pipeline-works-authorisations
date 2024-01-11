package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicence;
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

    if (form.getCrossingOwner() != null
        && form.getCrossingOwner().equals(CrossingOwner.PORTAL_ORGANISATION)) {
      ValidationUtils.rejectIfEmpty(
          errors, "blockOwnersOuIdList",
          "blockOwnersOuIdList.required",
          "Select a block owner"
      );
    }

    if (form.getCrossingOwner() != null && form.getCrossingOwner() != CrossingOwner.UNLICENSED) {
      if (licence == null) {
        errors.rejectValue("crossedBlockOwner", "crossedBlockOwner" + FieldValidationErrorCodes.INVALID.getCode(),
            "Unlicensed blocks cannot have an owner");
      }
    }

    if (form.getCrossingOwner() == CrossingOwner.UNLICENSED) {
      if (licence != null) {
        errors.rejectValue("crossedBlockOwner", "crossedBlockOwner" + FieldValidationErrorCodes.INVALID.getCode(),
            "Licensed blocks must have an owner");
      }
    }

    if (form.getBlockOwnersOuIdList() != null && !form.getBlockOwnersOuIdList().isEmpty()) {
      var validOrganisationUnitIdSet = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(
          form.getBlockOwnersOuIdList())
          .stream()
          .filter(PortalOrganisationUnit::isActive)
          .map(PortalOrganisationUnit::getOuId)
          .collect(Collectors.toSet());

      for (int i = 0; i < form.getBlockOwnersOuIdList().size(); i++) {
        if (!validOrganisationUnitIdSet.contains(form.getBlockOwnersOuIdList().get(i))) {
          var field = "blockOwnersOuIdList";
          errors.rejectValue(field, field + ".invalid", "Select a valid organisation unit");
        }
      }

    }
  }
}
