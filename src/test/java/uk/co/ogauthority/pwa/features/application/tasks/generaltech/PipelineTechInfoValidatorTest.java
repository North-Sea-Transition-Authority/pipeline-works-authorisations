package uk.co.ogauthority.pwa.features.application.tasks.generaltech;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineTechInfoValidatorTest {

  private PipelineTechInfoValidator validator;

  @Before
  public void setUp() {
    validator = new PipelineTechInfoValidator();
  }

  @Test
  public void validate_full_empty() {
    var form = new PipelineTechInfoForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).contains(
        entry("estimatedFieldLife", Set.of("estimatedFieldLife.required")),
        entry("pipelineDesignedToStandards", Set.of("pipelineDesignedToStandards.required")),
        entry("corrosionDescription", Set.of("corrosionDescription.required")),
        entry("plannedPipelineTieInPoints", Set.of("plannedPipelineTieInPoints.required"))
    );
  }

  @Test
  public void validate_full_ifNoHint() {
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
  public void validate_full_valid() {

    var form = getFullForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).isEmpty();

  }

  private PipelineTechInfoForm getFullForm() {
    var form = new PipelineTechInfoForm();
    form.setEstimatedFieldLife(5);
    form.setPipelineDesignedToStandards(true);
    form.setPipelineStandardsDescription("description");
    form.setCorrosionDescription("description");
    form.setPlannedPipelineTieInPoints(true);
    form.setTieInPointsDescription("description");
    return form;
  }

  @Test
  public void validate_full_pipelineStandardsDescription_notRequired() {
    var form = new PipelineTechInfoForm();
    form.setPipelineDesignedToStandards(false);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).doesNotContain(
        entry("pipelineDesignedToStandards", Set.of("pipelineDesignedToStandards.required"))
    );
  }

  @Test
  public void validate_full_tieInPointsDescription_notRequired() {
    var form = new PipelineTechInfoForm();
    form.setPlannedPipelineTieInPoints(false);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);
    assertThat(errorsMap).doesNotContain(
        entry("tieInPointsDescription", Set.of("tieInPointsDescription.required"))
    );
  }

  @Test
  public void validate_partial_valid() {

    var form = new PipelineTechInfoForm();

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errorsMap).isEmpty();

  }

  @Test
  public void validate_partial_someData_valid() {

    var form = new PipelineTechInfoForm();
    form.setPipelineDesignedToStandards(true);
    form.setPlannedPipelineTieInPoints(true);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errorsMap).isEmpty();

  }

  @Test
  public void validate_full_maxLength_invalid() {

    var form = getFullForm();
    form.setCorrosionDescription(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setPipelineStandardsDescription(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setTieInPointsDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errorsMap).containsOnly(
        entry("pipelineStandardsDescription", Set.of("pipelineStandardsDescription.maxLengthExceeded")),
        entry("corrosionDescription", Set.of("corrosionDescription.maxLengthExceeded")),
        entry("tieInPointsDescription", Set.of("tieInPointsDescription.maxLengthExceeded"))
    );

  }

  @Test
  public void validate_partial_maxLength_invalid() {

    var form = new PipelineTechInfoForm();
    form.setCorrosionDescription(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setPipelineStandardsDescription(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setTieInPointsDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.PARTIAL);

    assertThat(errorsMap).containsOnly(
        entry("pipelineStandardsDescription", Set.of("pipelineStandardsDescription.maxLengthExceeded")),
        entry("corrosionDescription", Set.of("corrosionDescription.maxLengthExceeded")),
        entry("tieInPointsDescription", Set.of("tieInPointsDescription.maxLengthExceeded"))
    );

  }

  @Test
  public void validate_estimatedFieldLife_negativeNumber_invalid() {
    var form = getFullForm();
    form.setEstimatedFieldLife(-1);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errorsMap).containsOnly(
        entry("estimatedFieldLife", Set.of("estimatedFieldLife.valueOutOfRange"))
    );
  }

  @Test
  public void validate_estimatedFieldLife_zero_invalid() {
    var form = getFullForm();
    form.setEstimatedFieldLife(0);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errorsMap).containsOnly(
        entry("estimatedFieldLife", Set.of("estimatedFieldLife.valueOutOfRange"))
    );
  }

  @Test
  public void validate_estimatedFieldLife_NullHydrogen() {
    var form = getFullForm();
    form.setCorrosionDescription(null);
    form.setEstimatedFieldLife(null);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, PwaResourceType.HYDROGEN);

    assertThat(errorsMap)
        .isNotEmpty()
        .doesNotContain(entry("estimatedFieldLife", Set.of("estimatedFieldLife.required")));
  }
}
