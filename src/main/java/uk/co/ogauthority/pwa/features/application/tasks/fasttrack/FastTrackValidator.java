package uk.co.ogauthority.pwa.features.application.tasks.fasttrack;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class FastTrackValidator implements SmartValidator {
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(FastTrackForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("this method has not been implemented. Use validate method with validation hints");
  }

  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {

    var form = (FastTrackForm) o;
    ValidationType validationType;
    if (validationHints[0] instanceof ValidationType) {
      validationType = (ValidationType) validationHints[0];
    } else {
      throw new UnsupportedOperationException("Expected first parameter to be: " + ValidationType.class.toString());
    }


    if (validationType.equals(ValidationType.FULL)
        && form.getAvoidEnvironmentalDisaster() == null
        && form.getSavingBarrels() == null
        && form.getProjectPlanning() == null
        && form.getHasOtherReason() == null) {
      errors.rejectValue("avoidEnvironmentalDisaster", "avoidEnvironmentalDisaster.noneSelected",
          "Select at least one reason for fast-tracking the application");
    }

    if (BooleanUtils.isTrue(form.getAvoidEnvironmentalDisaster())) {

      if (validationType.equals(ValidationType.FULL)) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "environmentalDisasterReason",
            "environmentalDisasterReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a reason for selecting avoiding environmental disaster");
      }

      ValidatorUtils.validateDefaultStringLength(
          errors, "environmentalDisasterReason", form::getEnvironmentalDisasterReason,
          "Reason for selecting avoiding environmental disaster");
    }

    if (BooleanUtils.isTrue(form.getSavingBarrels())) {

      if (validationType.equals(ValidationType.FULL)) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "savingBarrelsReason",
            "savingBarrelsReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a reason for selecting saving barrels");
      }

      ValidatorUtils.validateDefaultStringLength(
          errors, "savingBarrelsReason", form::getSavingBarrelsReason,
          "Reason for selecting saving barrels");
    }

    if (BooleanUtils.isTrue(form.getProjectPlanning())) {

      if (validationType.equals(ValidationType.FULL)) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectPlanningReason",
            "projectPlanningReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a reason for selecting project planning");
      }

      ValidatorUtils.validateDefaultStringLength(
          errors, "projectPlanningReason", form::getProjectPlanningReason,
          "Reason for selecting project planning");
    }

    if (BooleanUtils.isTrue(form.getHasOtherReason())) {

      if (validationType.equals(ValidationType.FULL)) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherReason",
            "otherReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a reason for selecting other reasons");
      }

      ValidatorUtils.validateDefaultStringLength(
          errors, "otherReason", form::getOtherReason,
          "Reason for selecting other reasons");
    }
  }
}
