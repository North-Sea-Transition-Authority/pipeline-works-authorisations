package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class PadPipelineTransferClaimValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return PadPipelineTransferClaimForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (PadPipelineTransferClaimForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "pipelineId",
        FieldValidationErrorCodes.REQUIRED.errorCode("pipelineId"),
        "Select a pipeline to transfer");

    var resourceType = PwaResourceType.valueOf((String)validationHints[0]);
    if (resourceType.equals(PwaResourceType.CCUS)) {
      ValidationUtils.rejectIfEmpty(
          errors,
          "lastIntelligentlyPigged",
          FieldValidationErrorCodes.REQUIRED.errorCode("lastIntelligentlyPigged"),
          "Enter the date the pipeline was last intelligently pigged"
      );
      ValidatorUtils.validateDatePickerDateIsPastOrPresent(
          "lastIntelligentlyPigged", "Last intelligently pigged date",
          form.getLastIntelligentlyPigged(),
          errors);

      ValidationUtils.rejectIfEmpty(
          errors,
          "compatibleWithTarget",
          FieldValidationErrorCodes.REQUIRED.errorCode("compatibleWithTarget"),
          "Confirm if the materials of the pipeline are compatible with CO2"
      );
    }
  }
}
