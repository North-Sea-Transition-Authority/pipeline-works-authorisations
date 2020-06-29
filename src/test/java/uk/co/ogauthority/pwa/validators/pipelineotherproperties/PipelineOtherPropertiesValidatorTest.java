package uk.co.ogauthority.pwa.validators.pipelineotherproperties;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineOtherPropertiesDataValidator;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineOtherPropertiesValidator;

@RunWith(MockitoJUnitRunner.class)
public class PipelineOtherPropertiesValidatorTest {

  private PipelineOtherPropertiesValidator validator;

  @Mock
  private PipelineOtherPropertiesDataValidator pipelineOtherPropertiesDataValidator;

  @Before
  public void setUp() {
    validator = new PipelineOtherPropertiesValidator(pipelineOtherPropertiesDataValidator);
  }


  @Test
  public void validate_formPhases_empty() {
    var form = new PipelineOtherPropertiesForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("oilPresent", Set.of("oilPresent.required"))
    );
  }

  @Test
  public void validate_formPhases_valid() {
    var form = new PipelineOtherPropertiesForm();
    form.setOilPresent(true);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(entry("oilPresent", Set.of("oilPresent.required")));
  }

  @Test
  public void validate_formPhases_valid_otherSelected() {
    var form = new PipelineOtherPropertiesForm();
    form.setOtherPresent(true);
    form.setOtherPhaseDescription("description");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(entry("oilPresent", Set.of("oilPresent.required")));
  }


  @Test
  public void validate_formPhases_invalid_otherSelected() {
    var form = new PipelineOtherPropertiesForm();
    form.setOtherPresent(true);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(entry("oilPresent", Set.of("oilPresent.required")));
  }






}