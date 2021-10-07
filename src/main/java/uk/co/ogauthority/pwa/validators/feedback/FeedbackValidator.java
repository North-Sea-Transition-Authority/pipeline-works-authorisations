package uk.co.ogauthority.pwa.validators.feedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.feedback.FeedbackForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.feedback.FeedbackService;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class FeedbackValidator implements Validator {


  @Autowired
  public FeedbackValidator() {}

  @Override
  public boolean supports(Class<?> clazz) {
    return FeedbackForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (FeedbackForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "serviceRating", FieldValidationErrorCodes.REQUIRED.errorCode("serviceRating"),
        "Enter how satisfied you were when using the service");

    ValidatorUtils.validateMaxStringLength(
        errors, "feedback", form::getFeedback, "Reason", FeedbackService.FEEDBACK_CHARACTER_LIMIT);
  }

}
