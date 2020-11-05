package uk.co.ogauthority.pwa.validators.pipelinetechinfo;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadDesignOpConditionsValidatorTest {

  @Spy
  private MinMaxInputValidator minMaxInputValidator;

  private PadDesignOpConditionsValidator validator;

  @Before
  public void setUp() {
    validator = new PadDesignOpConditionsValidator(minMaxInputValidator);
  }

  @Test
  public void validate_partial_noData() {
    var form = new DesignOpConditionsForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_full_noData() {
    var form = new DesignOpConditionsForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(result).containsOnlyKeys("flowrateDesignMinMax.maxValue", "flowrateDesignMinMax.minValue",
        "flowrateOpMinMax.maxValue",
        "flowrateOpMinMax.minValue", "pressureDesignMax",
        "pressureOpMinMax.maxValue", "pressureOpMinMax.minValue",
        "temperatureDesignMinMax.maxValue", "temperatureDesignMinMax.minValue",
        "temperatureOpMinMax.maxValue", "temperatureOpMinMax.minValue", "uvalueDesign");
  }
}