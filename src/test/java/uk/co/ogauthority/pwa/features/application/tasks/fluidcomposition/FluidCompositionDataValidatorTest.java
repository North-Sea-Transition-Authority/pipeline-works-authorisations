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
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class FluidCompositionDataValidatorTest {

  private FluidCompositionDataValidator validator;

  @Before
  public void setUp() {
    validator = new FluidCompositionDataValidator();
  }


  @Test
  public void validateForm_invalid() {
    var form = new FluidCompositionDataForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O);
    assertThat(errorsMap).contains(
        entry("fluidCompositionOption", Set.of("fluidCompositionOption.required"))
    );
  }

  @Test
  public void validateForm_valid() {
    var form = new FluidCompositionDataForm();
    form.setFluidCompositionOption(FluidCompositionOption.NONE);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O);
    assertThat(errorsMap).isEmpty();
  }


  @Test
  public void validateForm_molePercentageRequired_valid() {
    var form = new FluidCompositionDataForm();
    form.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    form.setMoleValue(BigDecimal.valueOf(0.2));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validateForm_molePercentageRequired_invalid() {
    var form = new FluidCompositionDataForm();
    form.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O);
    assertThat(errorsMap).contains(
        entry("moleValue", Set.of("moleValue.required"))
    );
  }

  @Test
  public void validateForm_molePercentageValue_tooSmall() {
    var form = new FluidCompositionDataForm();
    form.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    form.setMoleValue(BigDecimal.ZERO);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O);
    assertThat(errorsMap).contains(
        entry("moleValue", Set.of("moleValue.invalid"))
    );
  }

  @Test
  public void validateForm_molePercentageValue_tooLarge() {
    var form = new FluidCompositionDataForm();
    form.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    form.setMoleValue(BigDecimal.valueOf(101));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, Chemical.H2O);
    assertThat(errorsMap).contains(
        entry("moleValue", Set.of("moleValue.invalid"))
    );
  }





}