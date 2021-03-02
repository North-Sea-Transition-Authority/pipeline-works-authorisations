package uk.co.ogauthority.pwa.validators.publicnotice;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.FileUploadUtils;

@Service
public class PublicNoticeDocumentUpdateValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return UpdatePublicNoticeDocumentForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }



  @Override
  public void validate(Object target, Errors errors) {
    var form = (UpdatePublicNoticeDocumentForm) target;

    FileUploadUtils.validateMinFileLimit(form, errors, 1, "Upload a public notice document");
    FileUploadUtils.validateMaxFileLimit(form, errors, 1, "Upload a maximum of one file");

    if (!form.getUploadedFileWithDescriptionForms().isEmpty()) {
      ValidationUtils.rejectIfEmpty(
          errors,
          "uploadedFileWithDescriptionForms[0].uploadedFileDescription",
          FieldValidationErrorCodes.REQUIRED.errorCode("uploadedFileWithDescriptionForms[0].uploadedFileDescription"),
          "File must have a description");
    }

  }
}
