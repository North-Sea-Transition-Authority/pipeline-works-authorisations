package uk.co.ogauthority.pwa.util.forminputs.minmax;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.enums.validation.MinMaxValidationErrorCodes;

@Component
public class MinMaxInputValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(MinMaxInput.class);
  }

  @Override
  public void validate(Object o, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }


  @Override
  public void validate(Object o, Errors errors, Object... objects) {
    var minMaxInput = (MinMaxInput) o;
    var propertyName = (String) objects[0];
    var validationRequiredHints = (List<Object>) objects [1];

    if (!minMaxInput.isMinNumeric()) {
      errors.rejectValue("minValue", "minValue" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a valid minimum value for " + propertyName.toLowerCase());
    }
    if (!minMaxInput.isMaxNumeric()) {
      errors.rejectValue("maxValue", "maxValue" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a valid maximum value for " + propertyName.toLowerCase());
    }

    if (minMaxInput.isMinNumeric() && minMaxInput.isMaxNumeric()) {
      validateMinSmallerOrEqualToMax(errors, minMaxInput, propertyName);

      for (var validationRequired: validationRequiredHints) {
        if (validationRequired instanceof DecimalPlacesHint) {
          var decimalPlacesHint = (DecimalPlacesHint) validationRequired;
          validateDecimalPlaces(errors, minMaxInput, propertyName, decimalPlacesHint.getDecimalPlaces());

        } else if (validationRequired instanceof PositiveNumberHint) {
          validatePositiveNumber(errors, minMaxInput, propertyName);

        } else if (validationRequired instanceof IntegerHint) {
          validateInteger(errors, minMaxInput, propertyName);

        }
      }
    }

  }


  private void validateMinSmallerOrEqualToMax(Errors errors, MinMaxInput minMaxInput, String property) {
    if (!minMaxInput.minSmallerOrEqualToMax()) {
      errors.rejectValue("minValue", "minValue" + MinMaxValidationErrorCodes.MIN_LARGER_THAN_MAX.getCode(),
          "The minimum value must be smaller or equal to the maximum value for " + property.toLowerCase());
    }
  }


  private void validatePositiveNumber(Errors errors, MinMaxInput minMaxInput, String property) {
    if (!minMaxInput.isMinPositive()) {
      errors.rejectValue("minValue", "minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode(),
          "The minimum value must be a positive number for " + property.toLowerCase());
    }
    if (!minMaxInput.isMaxPositive()) {
      errors.rejectValue("maxValue", "maxValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode(),
          "The maximum value must be a positive number for " + property.toLowerCase());
    }
  }


  private void validateInteger(Errors errors, MinMaxInput minMaxInput, String property) {
    if (!minMaxInput.isMinInteger()) {
      errors.rejectValue("minValue", "minValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode(),
          "The minimum value must be a whole number for " + property.toLowerCase());
    }
    if (!minMaxInput.isMaxInteger()) {
      errors.rejectValue("maxValue", "maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode(),
          "The maximum value must be a whole number for " + property.toLowerCase());
    }
  }


  private void validateDecimalPlaces(Errors errors, MinMaxInput minMaxInput, String property, int maxDecimalPlaces) {
    if (!minMaxInput.minHasValidDecimalPlaces(maxDecimalPlaces)) {
      errors.rejectValue("minValue", "minValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode(),
          "The minimum value should not have more than " + maxDecimalPlaces + "dp for " + property.toLowerCase());
    }
    if (!minMaxInput.maxHasValidDecimalPlaces(maxDecimalPlaces)) {
      errors.rejectValue("maxValue", "maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode(),
          "The maximum value should not have more than " + maxDecimalPlaces + "dp for " + property.toLowerCase());
    }
  }



}

