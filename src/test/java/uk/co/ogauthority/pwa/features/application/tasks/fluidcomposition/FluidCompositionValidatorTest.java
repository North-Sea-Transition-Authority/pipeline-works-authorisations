package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class FluidCompositionValidatorTest {

  @Mock
  FluidCompositionFormValidator formValidator;

  FluidCompositionDataValidator dataValidator;

  FluidCompositionValidator validator;

  Errors errors;

  @Before
  public void setup() {
    dataValidator = new FluidCompositionDataValidator(new DecimalInputValidator());
    validator = new FluidCompositionValidator(dataValidator, formValidator);
  }

  @Test
  public void validate_dataValidationError_FormValidationSkipped() {
    var form = new FluidCompositionForm();
    var dataForm = new FluidCompositionDataForm();
    dataForm.setChemicalMeasurementType(ChemicalMeasurementType.PPMV_100K);
    form.addChemicalData(Chemical.HYDROCARBONS, dataForm);

    errors = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, errors, ValidationType.FULL);

    verify(formValidator, never()).validate(any(), any());
    assertThat(errors.getFieldError("chemicalDataFormMap[HYDROCARBONS].measurementValue").getCodes()).contains("measurementValue.required.form.chemicalDataFormMap[HYDROCARBONS].measurementValue");
  }
}
