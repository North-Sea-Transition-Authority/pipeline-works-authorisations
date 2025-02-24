package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;

@ExtendWith(MockitoExtension.class)
class FluidCompositionDataValidatorTest {

  private FluidCompositionDataValidator validator;

  @BeforeEach
  void setUp() {
    validator = new FluidCompositionDataValidator(new DecimalInputValidator());
  }


  @Test
  void validateForm_invalid() {
    var form = new FluidCompositionDataForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("chemicalMeasurementType", Set.of("chemicalMeasurementType.required"))
    );
  }

  @Test
  void validateForm_valid() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.NONE);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).isEmpty();
  }


  @Test
  void validateForm_molePercentageRequired_valid() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    form.setMeasurementValue(new DecimalInput(BigDecimal.valueOf(0.2)));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validateForm_molePercentageRequired_invalid() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("measurementValue", Set.of("measurementValue.required"))
    );
  }

  @Test
  void validateForm_molePercentageValue_tooSmall() {
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
  void validateForm_molePercentageValue_tooLarge() {
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
  void validateForm_molePercentageValue_tooManyDp() {
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
  void validateForm_ppmvRequired_invalid() {
    var form = new FluidCompositionDataForm();
    form.setChemicalMeasurementType(ChemicalMeasurementType.PPMV_100K);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
        ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("measurementValue", Set.of("measurementValue.required"))
    );
  }

  @Test
  void validateForm_ppmvValue_aboveLimit() {
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
  void validateForm_partialValidation_allowsEmptyForm() {
    var form = new FluidCompositionDataForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O,
      ValidationType.PARTIAL);
    assertThat(errorsMap).isEmpty();
  }

}
