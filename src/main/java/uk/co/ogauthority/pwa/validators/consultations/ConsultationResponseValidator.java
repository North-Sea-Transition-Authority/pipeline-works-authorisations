package uk.co.ogauthority.pwa.validators.consultations;

import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ConsultationResponseValidator implements Validator {

  private final ConsultationResponseDataValidator responseDataValidator;

  @Autowired
  public ConsultationResponseValidator(ConsultationResponseDataValidator responseDataValidator) {
    this.responseDataValidator = responseDataValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ConsultationResponseForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {

    var form = (ConsultationResponseForm) target;

    // sort form map by response group display order to ensure the validation errors are ordered correctly
    form.getResponseDataForms().entrySet().stream()
        .sorted(Comparator.comparing(e -> e.getKey().getDisplayOrder()))
        .forEach(e -> ValidatorUtils.invokeNestedValidator(errors, responseDataValidator,
            "responseDataForms[" + e.getKey() + "]", e.getValue(), e.getKey()));

    validateFileUploads(form, errors);
  }

  private void validateFileUploads(ConsultationResponseForm form, Errors errors) {
    boolean fileRequired = form.getResponseDataForms().values().stream()
        .map(ConsultationResponseDataForm::getConsultationResponseOption)
        .anyMatch(ConsultationResponseOption::requireDocumentUpload);

    if (fileRequired) {
      FileValidationUtils.validator()
          .withMinimumNumberOfFiles(1, "Upload at least one file to support your decision")
          .validate(errors, form.getUploadedFiles());
    }

    FileValidationUtils.validator().validate(errors, form.getUploadedFiles());
  }

}
