package uk.co.ogauthority.pwa.validators.techdrawings;

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
    return false;
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (PipelineDrawingForm) target;
    ValidationUtils.rejectIfEmpty(errors, "pipelineIds", "pipelineIds" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must select at least one pipeline");
    ValidationUtils.rejectIfEmpty(errors, "reference", "reference" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must enter a drawing reference");
  }
}
