package uk.co.ogauthority.pwa.validators.techinfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineTechInfoValidator;

@RunWith(MockitoJUnitRunner.class)
public class PipelineTechInfoValidatorTest {

  private PipelineTechInfoValidator validator;

  @Before
  public void setUp() {
    validator = new PipelineTechInfoValidator();
  }


  @Test
  public void validate_form_empty() {
    var form = new PipelineTechInfoForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("estimatedFieldLife", Set.of("estimatedFieldLife.required")),
        entry("pipelineDesignedToStandards", Set.of("pipelineDesignedToStandards.required")),
        entry("corrosionDescription", Set.of("corrosionDescription.required")),
        entry("plannedPipelineTieInPoints", Set.of("plannedPipelineTieInPoints.required"))
    );
  }

  @Test
  public void validate_form_valid() {
    var form = new PipelineTechInfoForm();
    form.setEstimatedFieldLife(5);
    form.setPipelineDesignedToStandards(true);
    form.setPipelineStandardsDescription("description");
    form.setCorrosionDescription("description");
    form.setPlannedPipelineTieInPoints(true);
    form.setTieInPointsDescription("description");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }


  @Test
  public void pipelineStandardsDescription_notRequired() {
    var form = new PipelineTechInfoForm();
    form.setPipelineDesignedToStandards(false);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(
        entry("pipelineDesignedToStandards", Set.of("pipelineDesignedToStandards.required"))
    );
  }

  @Test
  public void tieInPointsDescription_notRequired() {
    var form = new PipelineTechInfoForm();
    form.setPlannedPipelineTieInPoints(false);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(
        entry("tieInPointsDescription", Set.of("tieInPointsDescription.required"))
    );
  }





}