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
        entry("estimatedAssetLife", Set.of("estimatedAssetLife.required")),
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
    form.setEstimatedAssetLife(5);
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
  public void validate_estimatedAssetLife_negativeNumber_invalid() {
    var form = getFullForm();
    form.setEstimatedAssetLife(-1);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errorsMap).containsOnly(
        entry("estimatedAssetLife", Set.of("estimatedAssetLife.valueOutOfRange"))
    );
  }

  @Test
  public void validate_estimatedAssetLife_zero_invalid() {
    var form = getFullForm();
    form.setEstimatedAssetLife(0);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL);

    assertThat(errorsMap).containsOnly(
        entry("estimatedAssetLife", Set.of("estimatedAssetLife.valueOutOfRange"))
    );
  }

  @Test
  public void validate_estimatedAssetLife_NullHydrogen() {
    var form = getFullForm();
    form.setCorrosionDescription(null);
    form.setEstimatedAssetLife(null);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, PwaResourceType.HYDROGEN);

    assertThat(errorsMap)
        .isNotEmpty()
        .doesNotContain(entry("estimatedAssetLife", Set.of("estimatedAssetLife.required")));
  }

  @Test
  public void validate_estimatedAssetLife_NullCcus() {
    var form = getFullForm();
    form.setCorrosionDescription(null);
    form.setEstimatedAssetLife(null);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ValidationType.FULL, PwaResourceType.CCUS);

    assertThat(errorsMap)
        .isNotEmpty()
        .contains(entry("estimatedAssetLife", Set.of("estimatedAssetLife.required")));
  }
}
