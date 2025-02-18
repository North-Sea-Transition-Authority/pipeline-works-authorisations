package uk.co.ogauthority.pwa.validators.asbuilt;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class AsBuiltNotificationSubmissionValidatorTest {

  private AsBuiltNotificationSubmissionValidator asBuiltNotificationSubmissionValidator;

  @BeforeEach
  void setup() {
    asBuiltNotificationSubmissionValidator = new AsBuiltNotificationSubmissionValidator();
  }

  @Test
  void validate_form_empty() {
    var form = new AsBuiltNotificationSubmissionForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("asBuiltNotificationStatus", Set.of("asBuiltNotificationStatus" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_ogaReasonRequired() {
    var form = new AsBuiltNotificationSubmissionForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(true, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("asBuiltNotificationStatus", Set.of("asBuiltNotificationStatus" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("ogaSubmissionReason", Set.of("ogaSubmissionReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
        );
  }

  @Test
  void validate_form_newPipeline_noDateWorkCompleted_noDateBrughtIntoUse_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateWorkCompletedTimestampStr",
            Set.of("perConsentDateWorkCompletedTimestampStr" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("perConsentDateBroughtIntoUseTimestampStr",
            Set.of("perConsentDateBroughtIntoUseTimestampStr" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_newPipeline_noDateWorkCompleted_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setPerConsentDateBroughtIntoUseTimestampStr("31/08/2030");
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateWorkCompletedTimestampStr",
            Set.of("perConsentDateWorkCompletedTimestampStr" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_newPipeline_dateWorkCompletedAfterToday_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    form.setPerConsentDateWorkCompletedTimestampStr("10/10/3000");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.OUT_OF_USE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateWorkCompletedTimestampStr",
            Set.of("perConsentDateWorkCompletedTimestampStr" + FieldValidationErrorCodes.AFTER_TODAY.getCode()))
    );
  }

  @Test
  void validate_form_newPipeline_dateWorkCompletedAfterDateBroughtIntoUse__perConsent_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    form.setPerConsentDateWorkCompletedTimestampStr("10/10/2000");
    form.setPerConsentDateBroughtIntoUseTimestampStr("10/10/1000");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateBroughtIntoUseTimestampStr",
            Set.of("perConsentDateBroughtIntoUseTimestampStr" + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode()))
    );
  }

  @Test
  void validate_form_newPipeline_perConsent_validationPasses() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    form.setPerConsentDateWorkCompletedTimestampStr("10/10/2000");
    form.setPerConsentDateBroughtIntoUseTimestampStr("10/10/3000");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_form_newPipeline_notPerConsent_noDateWorkCompleted_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setNotPerConsentDateBroughtIntoUseTimestampStr("31/08/2030");
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.NOT_PER_CONSENT);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("notPerConsentDateWorkCompletedTimestampStr",
            Set.of("notPerConsentDateWorkCompletedTimestampStr" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_newPipeline_dateWorkCompletedAfterDateBroughtIntoUse__notPerConsent_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.NOT_PER_CONSENT);
    form.setNotPerConsentDateWorkCompletedTimestampStr("10/10/2000");
    form.setNotPerConsentDateBroughtIntoUseTimestampStr("10/10/1000");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("notPerConsentDateBroughtIntoUseTimestampStr",
            Set.of("notPerConsentDateBroughtIntoUseTimestampStr" + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode()))
    );
  }

  @Test
  void validate_form_newPipeline_notPerConsent_validationPasses() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.NOT_PER_CONSENT);
    form.setNotPerConsentDateWorkCompletedTimestampStr("10/10/2000");
    form.setNotPerConsentDateBroughtIntoUseTimestampStr("10/10/3000");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_form_newPipeline_invalidDateBroughtIntoUse_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    form.setPerConsentDateWorkCompletedTimestampStr("10/10/2000");
    form.setPerConsentDateBroughtIntoUseTimestampStr("ABC");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateBroughtIntoUseTimestampStr",
            Set.of("perConsentDateBroughtIntoUseTimestampStr" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_form_notNewPipeline_noDateWorkCompleted_noDateBrughtIntoUse_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.CONSENT_UPDATE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateWorkCompletedTimestampStr",
            Set.of("perConsentDateWorkCompletedTimestampStr" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_form_notNewPipeline_validationPasses() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    form.setPerConsentDateWorkCompletedTimestampStr("10/10/2000");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.OUT_OF_USE));

    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_form_invalidDate_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    form.setPerConsentDateWorkCompletedTimestampStr("abc");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(
        asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.CONSENT_UPDATE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateWorkCompletedTimestampStr",
            Set.of("perConsentDateWorkCompletedTimestampStr" + FieldValidationErrorCodes.INVALID.getCode()))
    );

  }

  @Test
  void validate_form_invalidStatus_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.MIGRATION);

    for (PipelineChangeCategory pipelineChangeCategory : PipelineChangeCategory.values()) {
      var errorsMap = ValidatorTestUtils.getFormValidationErrors(
          asBuiltNotificationSubmissionValidator,
          form,
          new AsBuiltNotificationSubmissionValidatorHint(false, pipelineChangeCategory));

      assertThat(errorsMap).containsOnly(
          entry("asBuiltNotificationStatus",
              Set.of("asBuiltNotificationStatus" + FieldValidationErrorCodes.INVALID.getCode()))
      );
    }


  }

}
