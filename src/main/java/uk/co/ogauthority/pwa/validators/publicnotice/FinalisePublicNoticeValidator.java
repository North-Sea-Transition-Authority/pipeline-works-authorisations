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


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }



  @Override
  public void validate(Object target, Errors errors) {
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
  }


}
