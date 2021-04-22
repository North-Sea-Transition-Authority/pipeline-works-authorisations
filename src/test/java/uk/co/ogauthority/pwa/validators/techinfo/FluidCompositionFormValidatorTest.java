package uk.co.ogauthority.pwa.validators.techinfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.FluidCompositionOption;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.FluidCompositionFormValidator;

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
  public void validateForm_invalid() {
    var compositionDataForm1 = new FluidCompositionDataForm();
    compositionDataForm1.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    compositionDataForm1.setMoleValue(BigDecimal.valueOf(11.1));
    var compositionDataForm2 = new FluidCompositionDataForm();
    compositionDataForm2.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    compositionDataForm2.setMoleValue(BigDecimal.valueOf(90.0));
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
    compositionDataForm1.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    compositionDataForm1.setMoleValue(BigDecimal.valueOf(101));
    var chemicalDataFormMap = Map.of(
        Chemical.H2O, compositionDataForm1
    );
    fluidCompositionForm.setChemicalDataFormMap(chemicalDataFormMap);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, fluidCompositionForm);
    assertThat(errorsMap).isEmpty();
  }

}
