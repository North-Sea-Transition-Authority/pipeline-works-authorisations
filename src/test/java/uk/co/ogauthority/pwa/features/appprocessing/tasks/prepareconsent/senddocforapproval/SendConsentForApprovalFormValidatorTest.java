package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class SendConsentForApprovalFormValidatorTest {

  private static final String CONSENTS_REVIEWED_ATTR = "parallelConsentsReviewedIfApplicable";
  private static final String COVER_LETTER_ATTR = "coverLetterText";

  @Mock
  private MailMergeService mailMergeService;

  private SendConsentForApprovalFormValidator validator;

  private final PwaApplication pwaApplication = new PwaApplication();

  @BeforeEach
  void setUp() throws Exception {

    validator = new SendConsentForApprovalFormValidator(mailMergeService);

  }

  @Test
  void supports_supportedClass() {
    assertThat(validator.supports(SendConsentForApprovalForm.class)).isTrue();
  }

  @Test
  void supports_notSupportedClass() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_allNull_noParallelConsents() {

    var form = new SendConsentForApprovalForm();
    var preApprovalChecks = PreSendForApprovalChecksViewTestUtil.createNoFailedChecksView();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, preApprovalChecks, pwaApplication);

    assertThat(errors).containsOnly(
        entry(COVER_LETTER_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(COVER_LETTER_ATTR)))
    );
  }

  @Test
  void validate_allNull_parallelConsent() {

    var form = new SendConsentForApprovalForm();
    var preApprovalChecks = PreSendForApprovalChecksViewTestUtil.createParallelConsentsChecksView();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, preApprovalChecks, pwaApplication);

    assertThat(errors).containsOnly(
        entry(CONSENTS_REVIEWED_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CONSENTS_REVIEWED_ATTR))),
        entry(COVER_LETTER_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(COVER_LETTER_ATTR)))
    );
  }

  @Test
  void validate_formHasValues_parallelConsent() {

    var form = new SendConsentForApprovalForm();
    form.setParallelConsentsReviewedIfApplicable(true);
    form.setCoverLetterText("some text");

    var preApprovalChecks = PreSendForApprovalChecksViewTestUtil.createParallelConsentsChecksView();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, preApprovalChecks, pwaApplication);

    assertThat(errors).isEmpty();

  }

  @Test
  void validate_manualMergeFieldsPresent_fail() {

    var form = new SendConsentForApprovalForm();
    form.setParallelConsentsReviewedIfApplicable(false);
    form.setCoverLetterText(String.format(
        "some text %s%s%s",
        MailMergeFieldType.MANUAL.getOpeningDelimiter(), "manualhere", MailMergeFieldType.MANUAL.getClosingDelimiter()));

    var preApprovalChecks = PreSendForApprovalChecksViewTestUtil.createNoFailedChecksView();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, preApprovalChecks, pwaApplication);

    assertThat(errors).containsOnly(
        entry(COVER_LETTER_ATTR, Set.of(FieldValidationErrorCodes.INVALID.errorCode(COVER_LETTER_ATTR)))
    );

  }

  @Test
  void validate_invalidMergeFieldsPresent_fail() {

    var form = new SendConsentForApprovalForm();
    form.setParallelConsentsReviewedIfApplicable(false);
    form.setCoverLetterText(String.format(
        "some text %s%s%s",
        MailMergeFieldType.AUTOMATIC.getOpeningDelimiter(), "TEST", MailMergeFieldType.AUTOMATIC.getClosingDelimiter()));

    var preApprovalChecks = PreSendForApprovalChecksViewTestUtil.createNoFailedChecksView();

    when(mailMergeService.validateMailMergeFields(pwaApplication, form.getCoverLetterText())).thenReturn(Set.of("TEST"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, preApprovalChecks, pwaApplication);

    assertThat(errors).containsOnly(
        entry(COVER_LETTER_ATTR, Set.of(FieldValidationErrorCodes.INVALID.errorCode(COVER_LETTER_ATTR)))
    );

  }

}