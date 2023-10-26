package uk.co.ogauthority.pwa.validators.publicnotice;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class FinalisePublicNoticeValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return FinalisePublicNoticeForm.class.equals(clazz);
  }


  /**
   * Validates finalise public notice information.
   * @param target the object that is to be validated
   * @param errors contextual state about the validation process
   * @param validationHints 0: boolean is reason for date required.
   */
  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (FinalisePublicNoticeForm) target;

    ValidatorUtils.validateDate(
        "start", "start",
        form.getStartDay(),
        form.getStartMonth(),
        form.getStartYear(),
        errors
    );

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "daysToBePublishedFor",
        FieldValidationErrorCodes.REQUIRED.errorCode("daysToBePublishedFor"),
        "Enter the number of days that this public notice should be published for");

    //Only needs validation if previously published public notice has had its date changed.
    var reasonRequired = (boolean) validationHints[0];
    if (reasonRequired) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dateChangeReason",
          FieldValidationErrorCodes.REQUIRED.errorCode("dateChangeReason"),
          "Give a reason for changing the date of a published public notice.");
    }
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, false);
  }

}
