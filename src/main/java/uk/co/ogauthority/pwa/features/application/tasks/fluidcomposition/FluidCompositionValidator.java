package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class FluidCompositionValidator implements SmartValidator {

  private final FluidCompositionDataValidator fluidCompositionDataValidator;
  private final FluidCompositionFormValidator fluidCompositionFormValidator;

  @Autowired
  public FluidCompositionValidator(FluidCompositionDataValidator fluidCompositionDataValidator,
                                   FluidCompositionFormValidator fluidCompositionFormValidator) {
    this.fluidCompositionDataValidator = fluidCompositionDataValidator;
    this.fluidCompositionFormValidator = fluidCompositionFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return FluidCompositionForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var fluidCompositionForm = (FluidCompositionForm) target;
    var validationType = (ValidationType) validationHints[0];

    var chemicalDataFormMap = fluidCompositionForm.getChemicalDataFormMap();

    if (validationType.equals(ValidationType.FULL)) {
      fluidCompositionFormValidator.validate(fluidCompositionForm, errors);
    }

    // sort form map by chemical display order to ensure the validation errors are ordered correctly
    chemicalDataFormMap.entrySet().stream()
        .sorted(Comparator.comparing(e -> e.getKey().getDisplayOrder()))
        .forEach(e -> ValidatorUtils.invokeNestedValidator(errors, fluidCompositionDataValidator,
            "chemicalDataFormMap[" + e.getKey() + "]", e.getValue(), e.getKey(), validationType));
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }
}
