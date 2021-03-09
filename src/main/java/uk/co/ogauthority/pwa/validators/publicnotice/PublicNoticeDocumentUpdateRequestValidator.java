package uk.co.ogauthority.pwa.validators.publicnotice;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDocumentUpdateRequestForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class PublicNoticeDocumentUpdateRequestValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return PublicNoticeDocumentUpdateRequestForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }



  @Override
  public void validate(Object target, Errors errors) {
    var form = (PublicNoticeDocumentUpdateRequestForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "comments",
        "comments" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Enter comments for the public notice document updates required");

    ValidatorUtils.validateDefaultStringLength(
        errors, "comments", form::getComments, "Comments for the document update");

  }


}
