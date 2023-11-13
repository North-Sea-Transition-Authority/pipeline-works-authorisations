package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.math.BigDecimal;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.validation.minmax.MinMaxLimit;

@Service
public class FluidCompositionFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return FluidCompositionForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, PwaResourceType.PETROLEUM);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var fluidCompositionForm = (FluidCompositionForm) target;
    var chemicalDataFormMap = fluidCompositionForm.getChemicalDataFormMap();
    var compositionLimits = getFluidCompositionLimits(getResourceTypeFromValidationHints(validationHints));

    BigDecimal totalComposition = chemicalDataFormMap.values().stream()
        .filter(fluidCompositionDataForm ->
            fluidCompositionDataForm.getChemicalMeasurementType() == ChemicalMeasurementType.MOLE_PERCENTAGE)
        .map(FluidCompositionDataForm::getMeasurementValue)
        .flatMap(moleValue -> moleValue.asBigDecimal().stream())
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalComposition.doubleValue() == 0) {
      errors.rejectValue("chemicalDataFormMap", "chemicalDataFormMap" + FieldValidationErrorCodes.REQUIRED.getCode(),
          String.format("Select ‘%s’ for at least one component", ChemicalMeasurementType.MOLE_PERCENTAGE.getDisplayText()));
    } else if (
        totalComposition.doubleValue() < compositionLimits.getMinimumLimit()
            || totalComposition.doubleValue() > compositionLimits.getMaximumLimit()) {
      errors.rejectValue("chemicalDataFormMap", "chemicalDataFormMap" + FieldValidationErrorCodes.VALUE_OUT_OF_RANGE.getCode(),
          String.format("The total fluid composition must be between %s%% and %s%% (current total composition %s%%)",
              compositionLimits.getMinimumLimit(),
              compositionLimits.getMaximumLimit(),
              totalComposition.doubleValue()));
    }

    if (fluidCompositionForm.getOtherInformation() != null && fluidCompositionForm.getOtherInformation().length() > 1000) {
      errors.rejectValue(
          "otherInformation",
          "otherInformation" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED,
          "Other Information cannot exceed 1000 characters"
      );
    }
  }

  private MinMaxLimit getFluidCompositionLimits(PwaResourceType resourceType) {
    switch (resourceType) {
      case PETROLEUM:
      case HYDROGEN:
        return new MinMaxLimit(99, 101);
      case CCUS:
        return new MinMaxLimit(95, 100);
      default:
        throw new IllegalStateException("Unexpected resource type when fetching composition tolerance limits: " +
            resourceType);
    }
  }

  private PwaResourceType getResourceTypeFromValidationHints(Object... validationHints) {
    return (PwaResourceType) Arrays.stream(validationHints)
        .filter(hints -> hints instanceof PwaResourceType)
        .findFirst()
        .orElse(null);
  }
}
