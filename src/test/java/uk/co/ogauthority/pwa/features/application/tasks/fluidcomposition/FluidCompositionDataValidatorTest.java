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
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class FluidCompositionDataValidatorTest {

  private FluidCompositionDataValidator validator;

  @Before
  public void setUp() {
    validator = new FluidCompositionDataValidator(new DecimalInputValidator());
  }


  @Test
  public void validateForm_invalid() {
    var form = new FluidCompositionDataForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("chemicalMeasurementType", Set.of("chemicalMeasurementType.required"))
    );
  }

  @Test
  public void validateForm_valid() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.NONE);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).isEmpty();
  }


  @Test
  public void validateForm_molePercentageRequired_valid() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    form.setMeasurementValue(new DecimalInput(BigDecimal.valueOf(0.2)));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validateForm_molePercentageRequired_invalid() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("measurementValue", Set.of("measurementValue.required"))
    );
  }

  @Test
  public void validateForm_molePercentageValue_tooSmall() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    form.setMeasurementValue(new DecimalInput(BigDecimal.ONE.negate()));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).contains(
      entry("measurementValue.value", Set.of("value.invalid"))
    );
  }

  @Test
  public void validateForm_molePercentageValue_tooLarge() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    form.setMeasurementValue(new DecimalInput(BigDecimal.valueOf(101)));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("measurementValue.value", Set.of("value.invalid"))
    );
  }

  @Test
  public void validateForm_molePercentageValue_tooManyDp() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    form.setMeasurementValue(new DecimalInput(new BigDecimal("99.011")));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).contains(
      entry("measurementValue.value", Set.of("value.maxDpExceeded"))
    );
  }

  @Test
  public void validateForm_ppmvRequired_invalid() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.PPMV_100K);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
        ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("measurementValue", Set.of("measurementValue.required"))
    );
  }

  @Test
  public void validateForm_ppmvValue_aboveLimit() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.PPMV_100K);
    form.setMeasurementValue(new DecimalInput(new BigDecimal(600000000)));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
        ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("measurementValue.value", Set.of("value.invalid"))
    );
  }

  @Test
  public void validateForm_partialValidation_allowsEmptyForm() {
    var form = new FluidCompositionDataForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.PARTIAL);
    assertThat(errorsMap).isEmpty();
  }

}
