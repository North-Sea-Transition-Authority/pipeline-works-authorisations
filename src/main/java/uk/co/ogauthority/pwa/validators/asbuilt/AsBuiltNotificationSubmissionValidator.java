package uk.co.ogauthority.pwa.validators.asbuilt;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class AsBuiltNotificationSubmissionValidator implements SmartValidator {

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, new Object[0]);
  }

  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    var form = (AsBuiltNotificationSubmissionForm) o;
    var validationHint = (AsBuiltNotificationSubmissionValidatorHint) validationHints[0];

    if (form.getAsBuiltNotificationStatus() == null) {
      errors.rejectValue("asBuiltNotificationStatus",
          "asBuiltNotificationStatus" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select the status of the pipeline");
    } else if (!AsBuiltNotificationStatus.getActiveStatusSet().contains(form.getAsBuiltNotificationStatus())) {
      errors.rejectValue("asBuiltNotificationStatus",
          "asBuiltNotificationStatus" + FieldValidationErrorCodes.INVALID.getCode(), "Select a valid option");
    } else {
      validateAsBuiltStatuses(form, errors, validationHint);
    }

    if (validationHint.isOgaUser()) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ogaSubmissionReason",
          "ogaSubmissionReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a reason for submitting the notification on behalf of the Holder");
    }

  }

  private void validateAsBuiltStatuses(AsBuiltNotificationSubmissionForm form, Errors errors,
                                       AsBuiltNotificationSubmissionValidatorHint hint) {
    switch (form.getAsBuiltNotificationStatus()) {
      case PER_CONSENT:

        boolean perConsentDateWorkCompletedValid = ValidatorUtils.validateDatePickerDateIsPastOrPresent(
            "perConsentDateWorkCompletedTimestampStr",
            "Date work completed",
            form.getPerConsentDateWorkCompletedTimestampStr(),
            errors);

        if (hint.getPipelineChangeCategory() == PipelineChangeCategory.NEW_PIPELINE) {

          boolean perConsentDateBroughtIntoUseValid = ValidatorUtils.validateDatePickerDateExistsAndIsValid(
              "perConsentDateBroughtIntoUseTimestampStr",
              "Date brought into use",
              form.getPerConsentDateBroughtIntoUseTimestampStr(),
              errors);

          if (perConsentDateWorkCompletedValid && perConsentDateBroughtIntoUseValid) {
            ValidatorUtils.validateDatePickerDateIsOnOrAfterComparisonDate("perConsentDateBroughtIntoUseTimestampStr",
                "Date pipeline brought into use", form.getPerConsentDateBroughtIntoUseTimestampStr(),
                DateUtils.datePickerStringToDate(form.getPerConsentDateWorkCompletedTimestampStr()), "date work completed", errors);
          }

        }

        break;

      case NOT_PER_CONSENT:

        boolean notPerConsentDateWorkCompletedValid = ValidatorUtils.validateDatePickerDateIsPastOrPresent(
            "notPerConsentDateWorkCompletedTimestampStr",
            "Date work completed",
            form.getNotPerConsentDateWorkCompletedTimestampStr(),
            errors);

        if (hint.getPipelineChangeCategory() == PipelineChangeCategory.NEW_PIPELINE) {

          boolean notPerConsentDateBroughtIntoUseValid = ValidatorUtils.validateDatePickerDateExistsAndIsValid(
              "notPerConsentDateBroughtIntoUseTimestampStr",
              "Date brought into use",
              form.getNotPerConsentDateBroughtIntoUseTimestampStr(),
              errors);

          if (notPerConsentDateWorkCompletedValid && notPerConsentDateBroughtIntoUseValid) {
            ValidatorUtils.validateDatePickerDateIsOnOrAfterComparisonDate("notPerConsentDateBroughtIntoUseTimestampStr",
                "Date pipeline brought into use", form.getNotPerConsentDateBroughtIntoUseTimestampStr(),
                DateUtils.datePickerStringToDate(form.getNotPerConsentDateWorkCompletedTimestampStr()), "date work completed", errors);
          }

        }

        break;

      case NOT_COMPLETED_IN_CONSENT_TIMEFRAME:
        ValidatorUtils.validateDatePickerDateExistsAndIsValid("notInConsentTimeframeDateWorkCompletedTimestampStr",
            "Estimated date work will be completed", form.getNotInConsentTimeframeDateWorkCompletedTimestampStr(), errors);
        break;
      default:
    }
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(AsBuiltNotificationSubmissionForm.class);
  }

}