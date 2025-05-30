package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;

class MedianLineAgreementValidatorTest {

  private final String VALID_EMAIL = "test@tester.london";
  private final String INVALID_EMAIL = "email @ email . com";

  private MedianLineAgreementValidator validator;
  private MedianLineAgreementsForm form = new MedianLineAgreementsForm();

  @BeforeEach
  void setUp() {
    validator = new MedianLineAgreementValidator();
    form.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
  }

  @Test
  void validate_partial_NoData() {
    form.setAgreementStatus(null);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_NoData() {
    form.setAgreementStatus(null);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).containsOnly(
        entry("agreementStatus", Set.of("agreementStatus.required"))
    );
  }

  @Test
  void validate_partial_lengthValidation() {

    form.setNegotiatorEmailIfCompleted(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setNegotiatorNameIfCompleted(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setNegotiatorEmailIfOngoing(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setNegotiatorNameIfOngoing(ValidatorTestUtils.overMaxDefaultCharLength());

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).satisfies(value -> {
      assertThat(value.get("negotiatorEmailIfCompleted")).contains("negotiatorEmailIfCompleted.maxLengthExceeded");
      assertThat(value.get("negotiatorNameIfCompleted")).contains("negotiatorNameIfCompleted.maxLengthExceeded");
      assertThat(value.get("negotiatorEmailIfOngoing")).contains("negotiatorEmailIfOngoing.maxLengthExceeded");
      assertThat(value.get("negotiatorNameIfOngoing")).contains("negotiatorNameIfOngoing.maxLengthExceeded");
    });
  }

  @Test
  void validate_full_lengthValidation() {

    form.setNegotiatorEmailIfCompleted(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setNegotiatorNameIfCompleted(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setNegotiatorEmailIfOngoing(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setNegotiatorNameIfOngoing(ValidatorTestUtils.overMaxDefaultCharLength());

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).satisfies(value -> {
      assertThat(value.get("negotiatorEmailIfCompleted")).contains("negotiatorEmailIfCompleted.maxLengthExceeded");
      assertThat(value.get("negotiatorNameIfCompleted")).contains("negotiatorNameIfCompleted.maxLengthExceeded");
      assertThat(value.get("negotiatorEmailIfOngoing")).contains("negotiatorEmailIfOngoing.maxLengthExceeded");
      assertThat(value.get("negotiatorNameIfOngoing")).contains("negotiatorNameIfOngoing.maxLengthExceeded");
    });

  }


  @Test
  void validate_full_validEmailNoErrors() {

    form.setNegotiatorEmailIfCompleted(VALID_EMAIL);
    form.setNegotiatorEmailIfOngoing(VALID_EMAIL);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_invalidEmailThenErrors() {

    form.setNegotiatorEmailIfCompleted(INVALID_EMAIL);
    form.setNegotiatorEmailIfOngoing(INVALID_EMAIL);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result.values())
        .isNotEmpty()
        .doesNotContain(Set.of("negotiatorEmailIfCompleted", "negotiatorEmailIfOngoing"));
  }

  @Test
  void validate_full_OngoingEmptyFields() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).containsOnly(
        entry("negotiatorNameIfOngoing", Set.of("negotiatorNameIfOngoing.required")),
        entry("negotiatorEmailIfOngoing", Set.of("negotiatorEmailIfOngoing.required"))
    );
  }

  @Test
  void validate_full_CompletedEmptyFields() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).containsOnly(
        entry("negotiatorNameIfCompleted", Set.of("negotiatorNameIfCompleted.required")),
        entry("negotiatorEmailIfCompleted", Set.of("negotiatorEmailIfCompleted.required"))
    );
  }

  @Test
  void validate_full_ValidOngoing() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    form.setNegotiatorNameIfOngoing("Name");
    form.setNegotiatorEmailIfOngoing(VALID_EMAIL);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_ValidCompleted() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    form.setNegotiatorNameIfCompleted("Name");
    form.setNegotiatorEmailIfCompleted(VALID_EMAIL);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).isEmpty();
  }

  @Test
  void validate_full_ValidNotCrossed() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).isEmpty();
  }
}