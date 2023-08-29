package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class PadPipelineTransferClaimValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return PadPipelineTransferClaimForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (PadPipelineTransferClaimForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "pipelineId",
        FieldValidationErrorCodes.REQUIRED.errorCode("pipelineId"),
        "Select a pipeline to transfer");
  }
}
