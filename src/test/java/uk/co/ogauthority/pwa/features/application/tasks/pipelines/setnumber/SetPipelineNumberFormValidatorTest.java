package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class SetPipelineNumberFormValidatorTest {
  private static final String PIPELINE_NUM_ATTR = "pipelineNumber";

  private static final int MIN = 1000;
  private static final int MAX = 2000;

  private static final PipelineId VALIDATED_PIPELINE_ID = new PipelineId(1);
  private static final PipelineId OTHER_PIPELINE_ID = new PipelineId(2);

  @Mock
  private PadPipelineService padPipelineService;

  private SetPipelineNumberFormValidator validator;

  private SetPipelineNumberForm form;
  private Pipeline pipeline;
  private PadPipeline validatedPadPipeline;
  private PwaApplicationDetail pwaApplicationDetail;
  private SetPipelineNumberValidationConfig config;


  @Before
  public void setUp() throws IllegalAccessException {
    validator = new SetPipelineNumberFormValidator(padPipelineService);

    form = new SetPipelineNumberForm();
    config = SetPipelineNumberValidationConfig.rangeCreate(MIN, MAX);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pipeline = new Pipeline(pwaApplicationDetail.getPwaApplication());
    pipeline.setId(VALIDATED_PIPELINE_ID.asInt());
    validatedPadPipeline = PadPipelineTestUtil.createPadPipeline(pwaApplicationDetail, pipeline, PipelineType.PRODUCTION_FLOWLINE);

    when(padPipelineService.findSubmittedOrDraftPipelinesWithPipelineNumber(any())).thenReturn(List.of());
  }

  @Test
  public void supports_withSupportedForm() {
    assertThat(validator.supports(SetPipelineNumberForm.class)).isTrue();
  }

  @Test
  public void supports_withOtherClass() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  public void validate_withNullNumber() {
    var errors = ValidatorTestUtils.getFormValidationErrors(
        validator, form, validatedPadPipeline, config);

    assertThat(errors).contains(
        entry(PIPELINE_NUM_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(PIPELINE_NUM_ATTR)))
    );
  }

  @Test
  public void validate_withInvalidNumberFormat() {
    var invalidFormatTests = List.of(
        "1000",  // missing prefix
        "1", // missing prefix
        "PLY", // good prefix, missing number
        "PLUX", // bad prefix, missing number
        "LP1000", // valid prefix letters in wrong order
        "PLU1000X", // invalid end char
        "PLU1000X11", // invalid bundle delimiter
        "PLU1000.zz", // invalid bundle decimals
        "1000.1", // no prefix
        "PLU1000.12345", // too many decimals
        "PLU1000." // missing bundle id
    );

    for(String invalidFormat : invalidFormatTests){

      form.setPipelineNumber(invalidFormat);

      var errors = ValidatorTestUtils.getFormValidationErrors(
          validator, form, validatedPadPipeline, config);

      try {
        assertThat(errors).contains(
            entry(PIPELINE_NUM_ATTR, Set.of(FieldValidationErrorCodes.INVALID.errorCode(PIPELINE_NUM_ATTR)))
        );
      } catch (AssertionError e) {
        throw new AssertionError("Failed at " + invalidFormat, e);
      }

    }
  }

  @Test
  public void validate_withValidNumberFormat_withinRange_uniqueNumber() {

    var validFormatTests = List.of(
        "PLU1000",  // good prefix
        "PL2000", // good prefix
        "PL1500.123"// has bundle
    );

    for(String validFormat : validFormatTests){

      form.setPipelineNumber(validFormat);

      var errors = ValidatorTestUtils.getFormValidationErrors(
          validator, form, validatedPadPipeline, config);

      try {
        assertThat(errors).isEmpty();

      } catch (AssertionError e) {
        throw new AssertionError("Failed at " + validFormat, e);
      }

    }
  }

  @Test
  public void validate_withValidNumberFormat_outOfRange() {

    var invalidFormatTests = List.of(
        "PLU999",
        "PLU3000"
    );

    for(String invalidFormat : invalidFormatTests){

      form.setPipelineNumber(invalidFormat);

      var errors = ValidatorTestUtils.getFormValidationErrors(
          validator, form, validatedPadPipeline, config);

      try {
        assertThat(errors).contains(
            entry(PIPELINE_NUM_ATTR, Set.of(FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode(PIPELINE_NUM_ATTR)))
        );

      } catch (AssertionError e) {
        throw new AssertionError("Failed at " + invalidFormat, e);
      }
    }
  }

  @Test
  public void validate_withValidNumberFormat_currentPipelineReferenceValidated_noOtherPipelinesHaveReference() {

    var pipelineNumber = "PL1234";

    form.setPipelineNumber(pipelineNumber);
    validatedPadPipeline.setPipelineRef(pipelineNumber);

    when(padPipelineService.findSubmittedOrDraftPipelinesWithPipelineNumber(pipelineNumber))
        .thenReturn(List.of(validatedPadPipeline));

    var errors = ValidatorTestUtils.getFormValidationErrors(
        validator, form, validatedPadPipeline, config);

    assertThat(errors).isEmpty();

  }

  @Test
  public void validate_withValidNumberFormat_otherPipelineHasReferenceValidated_noOtherPipelinesHaveReference() throws IllegalAccessException {

    var pipelineNumber = "PL1234";

    form.setPipelineNumber(pipelineNumber);


    var otherAppDetail = PwaApplicationTestUtil
        .createDefaultApplicationDetail(PwaApplicationType.INITIAL, 999, 999);

    var otherPipeline = new Pipeline(otherAppDetail.getPwaApplication());
    otherPipeline.setId(OTHER_PIPELINE_ID.asInt());
    var otherPadPipeline = PadPipelineTestUtil.createPadPipeline(otherAppDetail, otherPipeline, PipelineType.PRODUCTION_FLOWLINE);
    otherPadPipeline.setPipelineRef(pipelineNumber);

    when(padPipelineService.findSubmittedOrDraftPipelinesWithPipelineNumber(pipelineNumber))
        .thenReturn(List.of(otherPadPipeline));

    var errors = ValidatorTestUtils.getFormValidationErrors(
        validator, form, validatedPadPipeline, config);

    assertThat(errors).contains(
        entry(PIPELINE_NUM_ATTR, Set.of(FieldValidationErrorCodes.NOT_UNIQUE.errorCode(PIPELINE_NUM_ATTR)))
    );

  }

}