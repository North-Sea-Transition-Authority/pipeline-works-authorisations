package uk.co.ogauthority.pwa.validators.techdrawings;

import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class PipelineDrawingValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineDrawingForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (PipelineDrawingForm) target;
    ValidationUtils.rejectIfEmpty(errors, "pipelineIds", "pipelineIds" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must select at least one pipeline");
    ValidationUtils.rejectIfEmpty(errors, "reference", "reference" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must enter a drawing reference");
    if (ListUtils.emptyIfNull(form.getUploadedFileWithDescriptionForms()).size() > 1) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode(),
          "You must only upload a single drawing");
    }
  }
}
