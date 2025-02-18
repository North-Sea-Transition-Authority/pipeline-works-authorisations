package uk.co.ogauthority.pwa.features.application.tasks.crossings.types;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class CrossingTypesFormValidatorTest {

  private CrossingTypesFormValidator validator;

  private CrossingTypesForm form;

  @BeforeEach
  void setup() {
    validator = new CrossingTypesFormValidator();
    form = new CrossingTypesForm();
  }

  @Test
  void valid_petroleum() {
    form.setCablesCrossed(false);
    form.setPipelinesCrossed(false);
    form.setMedianLineCrossed(false);
    form.setCsaCrossed(false);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, PwaResourceType.PETROLEUM);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void valid_ccus() {
    form.setCablesCrossed(false);
    form.setPipelinesCrossed(false);
    form.setMedianLineCrossed(false);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, PwaResourceType.CCUS);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void invalid_petroleum() {
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, PwaResourceType.PETROLEUM);
    assertThat(errorsMap).hasSize(4);
    assertThat(errorsMap).contains(
        entry("cablesCrossed", Set.of("cablesCrossed" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("csaCrossed", Set.of("csaCrossed" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("medianLineCrossed", Set.of("medianLineCrossed" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelinesCrossed", Set.of("pipelinesCrossed" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void invalid_ccus() {
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, PwaResourceType.CCUS);
    assertThat(errorsMap).hasSize(3);
    assertThat(errorsMap).contains(
        entry("cablesCrossed", Set.of("cablesCrossed" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("medianLineCrossed", Set.of("medianLineCrossed" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("pipelinesCrossed", Set.of("pipelinesCrossed" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }
}
