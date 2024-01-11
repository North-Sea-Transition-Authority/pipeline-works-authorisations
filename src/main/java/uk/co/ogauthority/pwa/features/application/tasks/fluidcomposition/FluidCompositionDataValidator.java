package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType;
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
    var chemicalMeasurementType = form.getChemicalMeasurementType();

    if (validationType.equals(ValidationType.FULL)) {
      if (form.getChemicalMeasurementType() == null) {
        errors.rejectValue("chemicalMeasurementType", "chemicalMeasurementType" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Select a fluid composition option for " + chemical.getDisplayText());

      } else if (chemicalMeasurementType.hasNestedInput()) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
            errors,
            "measurementValue",
            "measurementValue" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a value for " + chemical.getDisplayText()
        );

        decimalInputValidator.invocationBuilder()
            .mustBeGreaterThanOrEqualTo(new BigDecimal(chemicalMeasurementType.getLowerLimit()))
            .mustBeLessThanOrEqualTo(new BigDecimal(chemicalMeasurementType.getUpperLimit()))
            .mustHaveNoMoreThanDecimalPlaces(2)
            .invokeNestedValidator(
              errors,
              "measurementValue",
              form.getMeasurementValue(),
              chemical.getDisplayText() + " value");
      }
    } else if (validationType.equals(ValidationType.PARTIAL)
        && form.getChemicalMeasurementType() != null
        && form.getChemicalMeasurementType().equals(ChemicalMeasurementType.MOLE_PERCENTAGE)) {
      decimalInputValidator
          .invocationBuilder()
          .partialValidate()
          .invokeNestedValidator(
              errors,
              "measurementValue",
              form.getMeasurementValue(),
              chemical.getDisplayText() + " value");
    }
  }
}

