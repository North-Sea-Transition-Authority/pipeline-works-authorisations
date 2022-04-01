package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;


@Service
public class FluidCompositionDataValidator implements SmartValidator {

  private final DecimalInputValidator decimalInputValidator;

  @Autowired
  public FluidCompositionDataValidator(
      DecimalInputValidator decimalInputValidator) {
    this.decimalInputValidator = decimalInputValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return FluidCompositionDataForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    var form = (FluidCompositionDataForm) o;
    var chemical = (Chemical) validationHints[0];
    var validationType = (ValidationType) validationHints[1];

    if (validationType.equals(ValidationType.FULL)) {
      if (form.getFluidCompositionOption() == null) {
        errors.rejectValue("fluidCompositionOption", "fluidCompositionOption" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Select a fluid composition option for " + chemical.getDisplayText());

      } else if (form.getFluidCompositionOption().equals(FluidCompositionOption.HIGHER_AMOUNT)) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "moleValue", "moleValue" + FieldValidationErrorCodes.REQUIRED.getCode(),
              "Enter a mole percentage for " + chemical.getDisplayText());

        decimalInputValidator.invocationBuilder()
            .mustBeGreaterThanZero()
            .mustBeLessThanOrEqualTo(new BigDecimal(100))
            .mustHaveNoMoreThanDecimalPlaces(2)
            .invokeNestedValidator(errors, "moleValue", form.getMoleValue(), chemical.getDisplayText() + " mole percentage");

      }
    } else if (validationType.equals(ValidationType.PARTIAL)
        && form.getFluidCompositionOption() != null
        && form.getFluidCompositionOption().equals(FluidCompositionOption.HIGHER_AMOUNT)) {
      decimalInputValidator.invocationBuilder()
        .partialValidate()
          .invokeNestedValidator(errors, "moleValue", form.getMoleValue(), chemical.getDisplayText() + " mole percentage");
    }
  }

}

