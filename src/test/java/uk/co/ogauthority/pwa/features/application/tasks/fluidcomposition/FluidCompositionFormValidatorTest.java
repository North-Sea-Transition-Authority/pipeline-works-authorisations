package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

@RunWith(MockitoJUnitRunner.class)
public class FluidCompositionFormValidatorTest {

  private FluidCompositionFormValidator validator;

  private FluidCompositionForm fluidCompositionForm;

  @Before
  public void setup() {
    validator  = new FluidCompositionFormValidator();
    fluidCompositionForm = new FluidCompositionForm();
  }

  @Test
  public void validateForm_invalid_atLeastOneValue() {
    fluidCompositionForm.setChemicalDataFormMap(Map.of());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, fluidCompositionForm);
    assertThat(errorsMap).contains(
        entry("chemicalDataFormMap", Set.of("chemicalDataFormMap" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validateForm_invalid_outOfRange() {
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
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, fluidCompositionForm);
    assertThat(errorsMap).contains(
        entry("chemicalDataFormMap", Set.of("chemicalDataFormMap" + FieldValidationErrorCodes.VALUE_OUT_OF_RANGE.getCode()))
    );
  }

  @Test
  public void validateForm_valid() {
    var compositionDataForm1 = new FluidCompositionDataForm();
    compositionDataForm1.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    compositionDataForm1.setMeasurementValue(new DecimalInput(BigDecimal.valueOf(101)));
    var chemicalDataFormMap = Map.of(
        Chemical.H2O, compositionDataForm1
    );
    fluidCompositionForm.setChemicalDataFormMap(chemicalDataFormMap);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, fluidCompositionForm);
    assertThat(errorsMap).isEmpty();
  }

}
