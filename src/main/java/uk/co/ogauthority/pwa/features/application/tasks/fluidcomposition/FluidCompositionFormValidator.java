package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class FluidCompositionFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return FluidCompositionForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var fluidCompositionForm = (FluidCompositionForm) target;

    var chemicalDataFormMap = fluidCompositionForm.getChemicalDataFormMap();

    BigDecimal totalComposition = chemicalDataFormMap.values().stream()
        .filter(fluidCompositionDataForm -> fluidCompositionDataForm.getFluidCompositionOption() == FluidCompositionOption.HIGHER_AMOUNT)
        .map(FluidCompositionDataForm::getMoleValue)
        .flatMap(moleValue -> moleValue.asBigDecimal().stream())
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalComposition.doubleValue() == 0) {
      errors.rejectValue("chemicalDataFormMap", "chemicalDataFormMap" + FieldValidationErrorCodes.REQUIRED.getCode(),
          String.format("Select ‘%s’ for at least one component", FluidCompositionOption.HIGHER_AMOUNT.getDisplayText()));
    } else if (totalComposition.doubleValue() < 99 || totalComposition.doubleValue() > 101) {
      errors.rejectValue("chemicalDataFormMap", "chemicalDataFormMap" + FieldValidationErrorCodes.VALUE_OUT_OF_RANGE.getCode(),
          String.format("The total fluid composition must be between 99%% and 101%% (current total composition %s%%)",
              totalComposition.doubleValue()));
    }
  }

}
