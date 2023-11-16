package uk.co.ogauthority.pwa.features.application.tasks.crossings.types;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class CrossingTypesFormValidator implements SmartValidator {
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(CrossingTypesForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, PwaResourceType.PETROLEUM);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var resourceType = (PwaResourceType) validationHints[0];
    var form = (CrossingTypesForm) target;
    ValidationUtils.rejectIfEmpty(errors, "pipelinesCrossed",
        "pipelinesCrossed" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select yes if a pipeline has been crossed");
    ValidationUtils.rejectIfEmpty(errors, "cablesCrossed",
        "cablesCrossed" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select yes if a cable has been crossed");
    ValidationUtils.rejectIfEmpty(errors, "medianLineCrossed",
        "medianLineCrossed" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select yes if any median line has been crossed");
    if (!resourceType.equals(PwaResourceType.CCUS)) {
      ValidationUtils.rejectIfEmpty(errors, "csaCrossed",
          "csaCrossed" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select yes if any carbon storage areas have been crossed");
    }
  }
}
