package uk.co.ogauthority.pwa.util.forminputs.minmax;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Component
public class MinMaxInputValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(MinMaxInput.class);
  }

  @Override
  public void validate(Object o, Errors errors) {
    validate(o, errors, new Object[0]);
  }

  @Override
  public void validate(Object o, Errors errors, Object... objects) {
    var minMaxInput = (MinMaxInput) o;
    var property = (OtherPipelineProperty) objects[0];
    var maxDecimalPlaces = (Integer) objects [1];

    if (minMaxInput.isMinEmpty()) {
      errors.rejectValue("minValue", "minValue" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a minimum value for " + property.getDisplayText());
    }

    if (minMaxInput.isMaxEmpty()) {
      errors.rejectValue("maxValue", "maxValue" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a maximum value for " + property.getDisplayText());
    }

    if (!minMaxInput.minSmallerOrEqualToMax()) {
      errors.rejectValue("minValue", "minValue" + FieldValidationErrorCodes.INVALID.getCode(),
          "The minimum value must be smaller or equal to the maximum value for " + property.getDisplayText());
    }

    if (!minMaxInput.minHasValidDecimalPlaces(maxDecimalPlaces)) {
      errors.rejectValue("minValue", "minValue" + FieldValidationErrorCodes.INVALID.getCode(),
          "The minimum value should not have more than " + maxDecimalPlaces + "dp for " + property.getDisplayText());
    }

    if (!minMaxInput.maxHasValidDecimalPlaces(maxDecimalPlaces)) {
      errors.rejectValue("maxValue", "maxValue" + FieldValidationErrorCodes.INVALID.getCode(),
          "The maximum value should not have more than " + maxDecimalPlaces + "dp for " + property.getDisplayText());
    }


  }



}
