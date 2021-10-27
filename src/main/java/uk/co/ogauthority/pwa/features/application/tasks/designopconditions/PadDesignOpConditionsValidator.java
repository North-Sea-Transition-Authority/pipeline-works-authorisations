package uk.co.ogauthority.pwa.features.application.tasks.designopconditions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.enums.validation.MinMaxValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.minmax.ByPassDefaultValidationHint;
import uk.co.ogauthority.pwa.util.forminputs.minmax.DecimalPlacesHint;
import uk.co.ogauthority.pwa.util.forminputs.minmax.DefaultValidationRule;
import uk.co.ogauthority.pwa.util.forminputs.minmax.IntegerHint;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;
import uk.co.ogauthority.pwa.util.forminputs.minmax.PositiveNumberHint;


@Service
public class PadDesignOpConditionsValidator implements SmartValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      PadDesignOpConditionsValidator.class);

  private final MinMaxInputValidator minMaxInputValidator;

  @Autowired
  public PadDesignOpConditionsValidator(
      MinMaxInputValidator minMaxInputValidator) {
    this.minMaxInputValidator = minMaxInputValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(DesignOpConditionsForm.class);
  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    var form = (DesignOpConditionsForm) o;
    var validationType = (ValidationType) validationHints[0];

    if (validationType.equals(ValidationType.FULL)) {
      ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "temperatureOpMinMax",
          form.getTemperatureOpMinMax(), "temperature operating conditions", List.of(), List.of(new IntegerHint()));

      ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "temperatureDesignMinMax",
          form.getTemperatureDesignMinMax(), "temperature design conditions", List.of(), List.of(new IntegerHint()));

      ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "pressureOpMinMax",
          form.getPressureOpMinMax(), "pressure operating conditions",
          List.of(new ByPassDefaultValidationHint(DefaultValidationRule.MIN_SMALLER_THAN_MAX)),
          List.of(new PositiveNumberHint(), new IntegerHint()));

      var pressureDesignMax = createBigDecimal(form.getPressureDesignMax());
      if (pressureDesignMax.isEmpty()) {
        errors.rejectValue("pressureDesignMax", "pressureDesignMax" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a valid value for pressure design conditions");
      } else {
        validatePositiveNumber(errors, pressureDesignMax.get(), "pressureDesignMax", "pressure design conditions");
      }

      ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "flowrateOpMinMax",
          form.getFlowrateOpMinMax(), "flowrate operating conditions",
          List.of(), List.of(new DecimalPlacesHint(2)));

      ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "flowrateDesignMinMax",
          form.getFlowrateDesignMinMax(), "flowrate design conditions",
          List.of(), List.of(new DecimalPlacesHint(2)));

      var uvalueDesign = createBigDecimal(form.getUvalueDesign());
      if (uvalueDesign.isEmpty()) {
        errors.rejectValue("uvalueDesign", "uvalueDesign" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a valid value for U-value design conditions");
      } else {
        validatePositiveNumber(errors, uvalueDesign.get(), "uvalueDesign", "U-value design conditions");
        validateDecimalPlaces(errors, uvalueDesign.get(), "uvalueDesign", "U-value design conditions", 1);
      }
    }
  }


  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }

  public Optional<BigDecimal> createBigDecimal(String valueStr) {
    try {
      var createdNum = valueStr != null ? new BigDecimal(valueStr) : null;
      return Optional.ofNullable(createdNum);
    } catch (NumberFormatException e) {
      LOGGER.debug("Could not convert u-value to a valid number. " + this.toString(), e);
      return Optional.empty();
    }
  }

  private void validatePositiveNumber(Errors errors, BigDecimal value, String formProperty, String inputRef) {
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      errors.rejectValue(formProperty, formProperty + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode(),
          "The value must be a positive number for " + inputRef);
    }
  }

  private void validateDecimalPlaces(Errors errors, BigDecimal value, String formProperty, String inputRef, int maxDecimalPlaces) {
    if (Math.max(0, value.stripTrailingZeros().scale()) > maxDecimalPlaces) {
      errors.rejectValue(formProperty, formProperty + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode(),
          "The value should not have more than " + maxDecimalPlaces + "dp for " +  inputRef);
    }
  }


}
