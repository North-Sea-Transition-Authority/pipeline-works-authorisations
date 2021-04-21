package uk.co.ogauthority.pwa.validators.pipelinetechinfo;

import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.FluidCompositionOption;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionForm;
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
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalComposition.doubleValue() < 99 || totalComposition.doubleValue() > 101) {
      errors.rejectValue("chemicalDataFormMap", "chemicalDataFormMap" + FieldValidationErrorCodes.VALUE_OUT_OF_RANGE.getCode(),
          String.format("The total fluid composition must be between 99%% and 101%% (currently %s%%)", totalComposition.doubleValue()));
    }
  }

}
