package uk.co.ogauthority.pwa.validators.pipelinetechinfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.enums.validation.MinMaxValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
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
    validate(o, errors);
  }


  @Override
  public void validate(Object target, Errors errors) {
    var form = (DesignOpConditionsForm) target;

    ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "temperatureOpMinMax",
        form.getTemperatureOpMinMax(), "temperature operating conditions", List.of(), List.of(new IntegerHint()));

    ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "temperatureDesignMinMax",
        form.getTemperatureDesignMinMax(), "temperature design conditions", List.of(), List.of(new IntegerHint()));

    ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "pressureOpInternalExternal",
        form.getPressureOpInternalExternal(), "pressure operating conditions",
        List.of(DefaultValidationRule.MIN_SMALLER_THAN_MAX), List.of(new PositiveNumberHint(), new IntegerHint()));

    ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "pressureDesignInternalExternal",
        form.getPressureDesignInternalExternal(), "pressure design conditions",
        List.of(DefaultValidationRule.MIN_SMALLER_THAN_MAX), List.of(new PositiveNumberHint(), new IntegerHint()));

    ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "flowrateOpMinMax",
        form.getFlowrateOpMinMax(), "flowrate operating conditions",
        List.of(), List.of(new PositiveNumberHint(), new DecimalPlacesHint(2)));

    ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator, "flowrateDesignMinMax",
        form.getFlowrateDesignMinMax(), "flowrate design conditions",
        List.of(), List.of(new PositiveNumberHint(), new DecimalPlacesHint(2)));


    var uvalueOp = createBigDecimal(form.getUvalueOp());
    if (uvalueOp.isEmpty()) {
      errors.rejectValue("uvalueOp", "uvalueOp" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a valid value for U-value operating conditions");
    } else {
      validatePositiveNumber(errors, uvalueOp.get(), "uvalueOp", "U-value operating conditions");
      validateDecimalPlaces(errors, uvalueOp.get(), "uvalueOp", "U-value operating conditions", 1);
    }

    var uvalueDesign = createBigDecimal(form.getUvalueDesign());
    if (uvalueDesign.isEmpty()) {
      errors.rejectValue("uvalueDesign", "uvalueDesign" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a valid value for U-value design conditions");
    } else {
      validatePositiveNumber(errors, uvalueDesign.get(), "uvalueDesign", "U-value design conditions");
      validateDecimalPlaces(errors, uvalueDesign.get(), "uvalueDesign", "U-value design conditions", 1);
    }

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
    if (value.compareTo(BigDecimal.ZERO) == -1) {
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
