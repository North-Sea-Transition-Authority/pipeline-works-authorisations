package uk.co.ogauthority.pwa.validators;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class FastTrackValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(FastTrackForm.class);
  }

  @Override
  public void validate(Object o, Errors errors) {
    var form = (FastTrackForm) o;
    if (form.getAvoidEnvironmentalDisaster() == null
        && form.getSavingBarrels() == null
        && form.getProjectPlanning() == null
        && form.getHasOtherReason() == null) {
      errors.rejectValue("avoidEnvironmentalDisaster", "avoidEnvironmentalDisaster.noneSelected",
          "Select at least one reason for fast-tracking the application");
    }

    if (BooleanUtils.isTrue(form.getAvoidEnvironmentalDisaster())) {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "environmentalDisasterReason",
          "environmentalDisasterReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a reason for selecting avoiding environmental disaster");

      ValidatorUtils.validateDefaultStringLength(
          errors, "environmentalDisasterReason", form::getEnvironmentalDisasterReason,
          "Reason for selecting avoiding environmental disaster");
    }

    if (BooleanUtils.isTrue(form.getSavingBarrels())) {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "savingBarrelsReason",
          "savingBarrelsReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a reason for selecting saving barrels");

      ValidatorUtils.validateDefaultStringLength(
          errors, "savingBarrelsReason", form::getSavingBarrelsReason,
          "Reason for selecting saving barrels");
    }

    if (BooleanUtils.isTrue(form.getProjectPlanning())) {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectPlanningReason",
          "projectPlanningReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a reason for selecting project planning");

      ValidatorUtils.validateDefaultStringLength(
          errors, "projectPlanningReason", form::getProjectPlanningReason,
          "Reason for selecting project planning");
    }

    if (BooleanUtils.isTrue(form.getHasOtherReason())) {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherReason",
          "otherReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a reason for selecting other reasons");

      ValidatorUtils.validateDefaultStringLength(
          errors, "otherReason", form::getOtherReason,
          "Reason for selecting other reasons");
    }
  }
}
