package uk.co.ogauthority.pwa.validators.publicnotice;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;

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

    FileValidationUtils.validator()
        .withMinimumNumberOfFiles(1, "Upload a public notice document")
        .withMaximumNumberOfFiles(1, "Upload a maximum of one file")
        .validate(errors, form.getUploadedFiles());
  }
}
