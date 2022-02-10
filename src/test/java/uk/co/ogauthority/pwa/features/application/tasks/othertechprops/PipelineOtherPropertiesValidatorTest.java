package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineOtherPropertiesValidatorTest {

  private PipelineOtherPropertiesValidator validator;
  private OtherPropertiesFormBuilder formBuilder = new OtherPropertiesFormBuilder();

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
        entry("phasesSelection[OIL]", Set.of("phasesSelection.required"))
    );
  }

  @Test
  public void validate_formPhases_valid() {
    var form = new PipelineOtherPropertiesForm();
    form.getPhasesSelection().put(PropertyPhase.OIL, "true");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(entry("phasesSelection", Set.of("phasesSelection.required")));
  }

  @Test
  public void validate_formPhases_valid_otherSelected() {
    var form = new PipelineOtherPropertiesForm();
    formBuilder.setPhasesFormData(form);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(entry("phasesSelection", Set.of("phasesSelection.required")));
  }


  @Test
  public void validate_formPhases_invalid_otherSelected() {
    var form = new PipelineOtherPropertiesForm();
    form.getPhasesSelection().put(PropertyPhase.OTHER, "true");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(entry("otherPhaseDescription", Set.of("otherPhaseDescription.required")));
  }






}