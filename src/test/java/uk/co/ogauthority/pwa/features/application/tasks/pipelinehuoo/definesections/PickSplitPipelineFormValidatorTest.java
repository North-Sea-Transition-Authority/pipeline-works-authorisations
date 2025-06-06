package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class PickSplitPipelineFormValidatorTest {
  private static final Integer UNKNOWN_PIPELINE_ID = 9999;
  private static final PipelineId PIPELINE_ID = new PipelineId(1);

  private static final HuooRole HUOO_ROLE = HuooRole.HOLDER;

  @Mock
  private PadPipelinesHuooService padPipelinesHuooService;

  @Mock
  private PipelineOverview splitablePipelineOverview;

  private PickSplitPipelineFormValidator pickSplitPipelineFormValidator;

  private PickSplitPipelineForm form;

  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    form = new PickSplitPipelineForm();
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pickSplitPipelineFormValidator = new PickSplitPipelineFormValidator(padPipelinesHuooService);
  }

  @Test
  void validate_whenAllNulls() {

    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        pickSplitPipelineFormValidator,
        form,
        pwaApplicationDetail,
        HUOO_ROLE
    );

    assertThat(validationErrors).containsExactly(
        entry(
            PickSplitPipelineFormValidator.PIPELINE_ID_ATTRIBUTE,
            Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(PickSplitPipelineFormValidator.PIPELINE_ID_ATTRIBUTE))
        ),
        entry(
            PickSplitPipelineFormValidator.NUMBER_OF_SECTIONS_ATTRIBUTE,
            Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(PickSplitPipelineFormValidator.NUMBER_OF_SECTIONS_ATTRIBUTE))
        )
    );

  }

  @Test
  void validate_whenInvalidPipelineProvided() {

    form.setPipelineId(UNKNOWN_PIPELINE_ID);
    form.setNumberOfSections(0);

    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        pickSplitPipelineFormValidator,
        form,
        pwaApplicationDetail,
        HUOO_ROLE
    );

    assertThat(validationErrors).containsExactly(
        entry(
            PickSplitPipelineFormValidator.PIPELINE_ID_ATTRIBUTE,
            Set.of(FieldValidationErrorCodes.INVALID.errorCode(PickSplitPipelineFormValidator.PIPELINE_ID_ATTRIBUTE))
        )

    );

  }

  @Test
  void validate_whenValidPipelineProvided_andNumberOfSectionsIsZero() {

    form.setPipelineId(PIPELINE_ID.asInt());
    form.setNumberOfSections(0);

    when(padPipelinesHuooService.getSplitablePipelineOverviewForApplication(pwaApplicationDetail, PIPELINE_ID))
        .thenReturn(Optional.of(splitablePipelineOverview));

    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        pickSplitPipelineFormValidator,
        form,
        pwaApplicationDetail,
        HUOO_ROLE
    );

    assertThat(validationErrors).containsExactly(
        entry(
            PickSplitPipelineFormValidator.NUMBER_OF_SECTIONS_ATTRIBUTE,
            Set.of(FieldValidationErrorCodes.INVALID.errorCode(PickSplitPipelineFormValidator.NUMBER_OF_SECTIONS_ATTRIBUTE))
        )

    );

  }

  @Test
  void validate_whenValidPipelineProvided_andNumberOfSectionsIsMoreThanZero() {

    form.setPipelineId(PIPELINE_ID.asInt());
    form.setNumberOfSections(1);

    when(padPipelinesHuooService.getSplitablePipelineOverviewForApplication(pwaApplicationDetail, PIPELINE_ID))
        .thenReturn(Optional.of(splitablePipelineOverview));

    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        pickSplitPipelineFormValidator,
        form,
        pwaApplicationDetail,
        HUOO_ROLE
    );

    assertThat(validationErrors).isEmpty();
  }

}