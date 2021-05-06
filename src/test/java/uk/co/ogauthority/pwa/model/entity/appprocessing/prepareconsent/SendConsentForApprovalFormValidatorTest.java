package uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent.SendConsentForApprovalForm;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.PreSendForApprovalChecksViewTestUtil;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class SendConsentForApprovalFormValidatorTest {

  private static final String CONSENTS_REVIEWED_ATTR = "parallelConsentsReviewedIfApplicable";
  private static final String COVER_LETTER_ATTR = "coverLetterText";

  private SendConsentForApprovalFormValidator validator;

  @Before
  public void setUp() throws Exception {

    validator = new SendConsentForApprovalFormValidator();
  }

  @Test
  public void supports_supportedClass() {
    assertThat(validator.supports(SendConsentForApprovalForm.class)).isTrue();
  }

  @Test
  public void supports_notSupportedClass() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  public void validate_allNull_noParallelConsents() {

    var form = new SendConsentForApprovalForm();
    var preApprovalChecks = PreSendForApprovalChecksViewTestUtil.createNoFailedChecksView();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, preApprovalChecks);

    assertThat(errors).containsOnly(
        entry(COVER_LETTER_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(COVER_LETTER_ATTR)))
    );
  }

  @Test
  public void validate_allNull_parallelConsent() {

    var form = new SendConsentForApprovalForm();
    var preApprovalChecks = PreSendForApprovalChecksViewTestUtil.createParallelConsentsChecksView();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, preApprovalChecks);

    assertThat(errors).containsOnly(
        entry(CONSENTS_REVIEWED_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(CONSENTS_REVIEWED_ATTR))),
        entry(COVER_LETTER_ATTR, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(COVER_LETTER_ATTR)))
    );
  }

  @Test
  public void validate_formHasValues_parallelConsent() {

    var form = new SendConsentForApprovalForm();
    form.setParallelConsentsReviewedIfApplicable(true);
    form.setCoverLetterText("some text");

    var preApprovalChecks = PreSendForApprovalChecksViewTestUtil.createParallelConsentsChecksView();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, preApprovalChecks);

    assertThat(errors).isEmpty();
  }
}