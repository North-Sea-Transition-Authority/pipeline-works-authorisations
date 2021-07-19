package uk.co.ogauthority.pwa.validators.asbuilt;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltNotificationSubmissionValidatorTest {

  private AsBuiltNotificationSubmissionValidator asBuiltNotificationSubmissionValidator;

  @Before
  public void setup() {
    asBuiltNotificationSubmissionValidator = new AsBuiltNotificationSubmissionValidator();
  }

  @Test
  public void validate_form_empty() {
    var form = new AsBuiltNotificationSubmissionForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("asBuiltNotificationStatus", Set.of("asBuiltNotificationStatus" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_form_ogaReasonRequired() {
    var form = new AsBuiltNotificationSubmissionForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(true, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("asBuiltNotificationStatus", Set.of("asBuiltNotificationStatus" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("ogaSubmissionReason", Set.of("ogaSubmissionReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
        );
  }

  @Test
  public void validate_form_newPipeline_noDateLaid_noDateBrughtIntoUse_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.NEW_PIPELINE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateLaidTimestampStr",
            Set.of("perConsentDateLaidTimestampStr" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("perConsentDateBroughtIntoUseTimestampStr",
            Set.of("perConsentDateBroughtIntoUseTimestampStr" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_form_notNewPipeline_noDateLaid_noDateBrughtIntoUse_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.CONSENT_UPDATE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateLaidTimestampStr",
            Set.of("perConsentDateLaidTimestampStr" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_form_invalidDate_validationFails() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    form.setPerConsentDateLaidTimestampStr("abc");

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(
        asBuiltNotificationSubmissionValidator, form,
        new AsBuiltNotificationSubmissionValidatorHint(false, PipelineChangeCategory.CONSENT_UPDATE));

    assertThat(errorsMap).containsOnly(
        entry("perConsentDateLaidTimestampStr",
            Set.of("perConsentDateLaidTimestampStr" + FieldValidationErrorCodes.INVALID.getCode()))
    );

  }

  @Test
  public void validate_form_invalidStatus_validationFails() {
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
