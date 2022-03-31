package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;


@Service
public class PipelineOtherPropertiesValidator implements SmartValidator {

  private PipelineOtherPropertiesDataValidator pipelineOtherPropertiesDataValidator;

  @Autowired
  public PipelineOtherPropertiesValidator(
      PipelineOtherPropertiesDataValidator pipelineOtherPropertiesDataValidator) {
    this.pipelineOtherPropertiesDataValidator = pipelineOtherPropertiesDataValidator;
  }


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineOtherPropertiesForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    this.validate(target, errors, ValidationType.FULL);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var pipelineOtherPropertiesForm = (PipelineOtherPropertiesForm) target;
    var propertyDataFormMap = pipelineOtherPropertiesForm.getPropertyDataFormMap();
    var validationType = (ValidationType) validationHints[0];

    if (validationType.equals(ValidationType.FULL)) {
      if (pipelineOtherPropertiesForm.getPhasesSelection().isEmpty()) {
        errors.rejectValue("phasesSelection[OIL]", "phasesSelection" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Select at least one phase");
      }
      if (pipelineOtherPropertiesForm.getPhasesSelection().containsKey(PropertyPhase.OTHER)) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherPhaseDescription", "otherPhaseDescription.required",
            "Enter the other phase present");
      }
    }
    for (var propertyEntry: propertyDataFormMap.entrySet()) {
      ValidatorUtils.invokeNestedValidator(errors, pipelineOtherPropertiesDataValidator,
          "propertyDataFormMap[" + propertyEntry.getKey() + "]", propertyEntry.getValue(), propertyEntry.getKey(), validationType);
    }
  }



}
