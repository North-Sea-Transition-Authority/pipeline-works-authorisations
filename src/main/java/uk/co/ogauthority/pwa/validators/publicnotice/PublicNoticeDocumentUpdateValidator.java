package uk.co.ogauthority.pwa.validators.publicnotice;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

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

    FileUploadUtils.validateFiles(form, errors, List.of(MandatoryUploadValidation.class), "Upload a public notice document");
    FileUploadUtils.validateMaxFileLimit(form, errors, 1, "Upload a maximum of one file");
  }
}
