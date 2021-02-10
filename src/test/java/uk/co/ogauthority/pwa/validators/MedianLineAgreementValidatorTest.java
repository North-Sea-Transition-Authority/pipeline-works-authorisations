package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;

public class MedianLineAgreementValidatorTest {

  private final String VALID_EMAIL = "test@tester.london";
  private final String INVALID_EMAIL = "email @ email . com";

  private MedianLineAgreementValidator validator;
  private MedianLineAgreementsForm form = new MedianLineAgreementsForm();

  @Before
  public void setUp() {
    validator = new MedianLineAgreementValidator();
    form.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
  }

  @Test
  public void validate_partial_NoData() {
    form.setAgreementStatus(null);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_full_NoData() {
    form.setAgreementStatus(null);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).containsOnly(
        entry("agreementStatus", Set.of("agreementStatus.required"))
    );
  }

  @Test
  public void validate_partial_lengthValidation() {

    form.setNegotiatorEmailIfCompleted(ValidatorTestUtils.over4000Chars());
    form.setNegotiatorNameIfCompleted(ValidatorTestUtils.over4000Chars());
    form.setNegotiatorEmailIfOngoing(ValidatorTestUtils.over4000Chars());
    form.setNegotiatorNameIfOngoing(ValidatorTestUtils.over4000Chars());

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).satisfies(value -> {
      assertThat(value.get("negotiatorEmailIfCompleted")).contains("negotiatorEmailIfCompleted.maxLengthExceeded");
      assertThat(value.get("negotiatorNameIfCompleted")).contains("negotiatorNameIfCompleted.maxLengthExceeded");
      assertThat(value.get("negotiatorEmailIfOngoing")).contains("negotiatorEmailIfOngoing.maxLengthExceeded");
      assertThat(value.get("negotiatorNameIfOngoing")).contains("negotiatorNameIfOngoing.maxLengthExceeded");
    });
  }

  @Test
  public void validate_full_lengthValidation() {

    form.setNegotiatorEmailIfCompleted(ValidatorTestUtils.over4000Chars());
    form.setNegotiatorNameIfCompleted(ValidatorTestUtils.over4000Chars());
    form.setNegotiatorEmailIfOngoing(ValidatorTestUtils.over4000Chars());
    form.setNegotiatorNameIfOngoing(ValidatorTestUtils.over4000Chars());

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).satisfies(value -> {
      assertThat(value.get("negotiatorEmailIfCompleted")).contains("negotiatorEmailIfCompleted.maxLengthExceeded");
      assertThat(value.get("negotiatorNameIfCompleted")).contains("negotiatorNameIfCompleted.maxLengthExceeded");
      assertThat(value.get("negotiatorEmailIfOngoing")).contains("negotiatorEmailIfOngoing.maxLengthExceeded");
      assertThat(value.get("negotiatorNameIfOngoing")).contains("negotiatorNameIfOngoing.maxLengthExceeded");
    });

  }


  @Test
  public void validate_full_validEmailNoErrors() {

    form.setNegotiatorEmailIfCompleted(VALID_EMAIL);
    form.setNegotiatorEmailIfOngoing(VALID_EMAIL);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_full_invalidEmailThenErrors() {

    form.setNegotiatorEmailIfCompleted(INVALID_EMAIL);
    form.setNegotiatorEmailIfOngoing(INVALID_EMAIL);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result.values())
        .isNotEmpty()
        .doesNotContain(Set.of("negotiatorEmailIfCompleted", "negotiatorEmailIfOngoing"));
  }

  @Test
  public void validate_full_OngoingEmptyFields() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).containsOnly(
        entry("negotiatorNameIfOngoing", Set.of("negotiatorNameIfOngoing.required")),
        entry("negotiatorEmailIfOngoing", Set.of("negotiatorEmailIfOngoing.required"))
    );
  }

  @Test
  public void validate_full_CompletedEmptyFields() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).containsOnly(
        entry("negotiatorNameIfCompleted", Set.of("negotiatorNameIfCompleted.required")),
        entry("negotiatorEmailIfCompleted", Set.of("negotiatorEmailIfCompleted.required"))
    );
  }

  @Test
  public void validate_full_ValidOngoing() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    form.setNegotiatorNameIfOngoing("Name");
    form.setNegotiatorEmailIfOngoing(VALID_EMAIL);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_full_ValidCompleted() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    form.setNegotiatorNameIfCompleted("Name");
    form.setNegotiatorEmailIfCompleted(VALID_EMAIL);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_full_ValidNotCrossed() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, FullValidation.class);
    assertThat(result).isEmpty();
  }
}