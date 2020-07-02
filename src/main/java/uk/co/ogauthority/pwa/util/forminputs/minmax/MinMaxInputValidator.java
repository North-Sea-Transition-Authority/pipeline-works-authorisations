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

    if (!minMaxInput.isMinNumeric() || !minMaxInput.isMaxNumeric()) {
      errors.rejectValue("maxValue", "maxValue" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a valid minimum and maximum value for " + propertyName.toLowerCase());
    } else {
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
      errors.rejectValue("maxValue", "maxValue" + MinMaxValidationErrorCodes.MIN_LARGER_THAN_MAX.getCode(),
          "The minimum value must be smaller or equal to the maximum value for " + property.toLowerCase());
    }
  }


  private void validatePositiveNumber(Errors errors, MinMaxInput minMaxInput, String property) {
    if (!minMaxInput.isMinPositive() || !minMaxInput.isMaxPositive()) {
      errors.rejectValue("maxValue", "maxValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode(),
          "The minimum and maximum values must be a positive number for " + property.toLowerCase());
    }
  }


  private void validateInteger(Errors errors, MinMaxInput minMaxInput, String property) {
    if (!minMaxInput.isMinInteger() || !minMaxInput.isMaxInteger()) {
      errors.rejectValue("maxValue", "maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode(),
          "The minimum and maximum values must be a whole number for " + property.toLowerCase());
    }
  }


  private void validateDecimalPlaces(Errors errors, MinMaxInput minMaxInput, String property, int maxDecimalPlaces) {
    if (!minMaxInput.minHasValidDecimalPlaces(maxDecimalPlaces) || !minMaxInput.maxHasValidDecimalPlaces(maxDecimalPlaces)) {
      errors.rejectValue("maxValue", "maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode(),
          "The minimum and maximum values should not have more than " + maxDecimalPlaces + "dp for " + property.toLowerCase());
    }
  }



}

