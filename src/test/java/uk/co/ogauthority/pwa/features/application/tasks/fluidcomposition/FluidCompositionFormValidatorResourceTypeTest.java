package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

@RunWith(Parameterized.class)
public class FluidCompositionFormValidatorResourceTypeTest {

  private FluidCompositionFormValidator validator;
  private FluidCompositionForm fluidCompositionForm;

  private final PwaResourceType resourceType;

  public FluidCompositionFormValidatorResourceTypeTest(PwaResourceType resourceType) {
    this.resourceType = resourceType;
  }

  @Before
  public void setup() {
    validator  = new FluidCompositionFormValidator();
    fluidCompositionForm = new FluidCompositionForm();
  }

  @Test
  public void validateForm_whenMeasurementsOutOfRange_thenHasErrors() {
    var expectedFluidCompositionLimits = validator.getFluidCompositionLimits(resourceType);

    var compositionDataForm1 = new FluidCompositionDataForm();
    compositionDataForm1.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    compositionDataForm1.setMeasurementValue(new DecimalInput(BigDecimal.valueOf(11.1)));
    var compositionDataForm2 = new FluidCompositionDataForm();
    compositionDataForm2.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    compositionDataForm2.setMeasurementValue(new DecimalInput(BigDecimal.valueOf(90.0)));
    var chemicalDataFormMap = Map.of(
        Chemical.H2O, compositionDataForm1,
        Chemical.C2, compositionDataForm2
    );
    fluidCompositionForm.setChemicalDataFormMap(chemicalDataFormMap);

    var bindingResult = new BeanPropertyBindingResult(fluidCompositionForm, "form");
    ValidationUtils.invokeValidator(validator, fluidCompositionForm, bindingResult, resourceType);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(errorsMap).contains(
        entry("chemicalDataFormMap", Set.of("chemicalDataFormMap" + FieldValidationErrorCodes.VALUE_OUT_OF_RANGE.getCode()))
    );

    Map<String, Set<String>> errorMessages = ValidatorTestUtils.extractErrorMessages(bindingResult);
    assertThat(errorMessages).contains(
        entry("chemicalDataFormMap",
            Set.of(String.format("The total fluid composition must be between %s%% and %s%% (current total composition %s%%)",
                expectedFluidCompositionLimits.getMinimumLimit(),
                expectedFluidCompositionLimits.getMaximumLimit(),
                101.1)))
    );
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection getParameters() {
   return PwaResourceType.getAll();
  }
}