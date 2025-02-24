package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class PipelineOtherPropertiesValidatorTest {

  private PipelineOtherPropertiesValidator validator;
  private OtherPropertiesFormBuilder formBuilder = new OtherPropertiesFormBuilder();

  @Mock
  private PipelineOtherPropertiesDataValidator pipelineOtherPropertiesDataValidator;

  @BeforeEach
  void setUp() {
    validator = new PipelineOtherPropertiesValidator(pipelineOtherPropertiesDataValidator);
  }


  @Test
  void validate_formPhases_empty() {
    var form = new PipelineOtherPropertiesForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("phasesSelection", Set.of("phasesSelection.required"))
    );
  }

  @Test
  void validate_formPhases_emptyCCUS() {
    var form = new PipelineOtherPropertiesForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, PwaResourceType.CCUS);
    assertThat(errorsMap).contains(
        entry("phase", Set.of("phase.required"))
    );
  }

  @Test
  void validate_formPhases_valid() {
    var form = new PipelineOtherPropertiesForm();
    form.getPhasesSelection().put(PropertyPhase.OIL, "true");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(entry("phasesSelection", Set.of("phasesSelection.required")));
  }

  @Test
  void validate_formPhases_validemptyCCUS() {
    var form = new PipelineOtherPropertiesForm();
    form.setOtherPhaseDescription("Plasma phase");
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, PwaResourceType.CCUS);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_formPhases_valid_otherSelected() {
    var form = new PipelineOtherPropertiesForm();
    formBuilder.setPhasesFormData(form);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(entry("phasesSelection", Set.of("phasesSelection.required")));
  }


  @Test
  void validate_formPhases_invalid_otherSelected() {
    var form = new PipelineOtherPropertiesForm();
    form.getPhasesSelection().put(PropertyPhase.OTHER, "true");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(entry("otherPhaseDescription", Set.of("otherPhaseDescription.required")));
  }






}
