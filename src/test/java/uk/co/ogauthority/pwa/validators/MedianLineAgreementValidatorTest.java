package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

public class MedianLineAgreementValidatorTest {

  private MedianLineAgreementValidator validator;

  @Before
  public void setUp() {
    validator = new MedianLineAgreementValidator();
  }

  @Test
  public void validate_NoData() {
    var form = new MedianLineAgreementsForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).containsOnly(
        entry("agreementStatus", Set.of("agreementStatus.required"))
    );
  }

  @Test
  public void validate_OngoingEmptyFields() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).containsOnly(
        entry("negotiatorNameIfOngoing", Set.of("negotiatorNameIfOngoing.required")),
        entry("negotiatorEmailIfOngoing", Set.of("negotiatorEmailIfOngoing.required"))
    );
  }

  @Test
  public void validate_CompletedEmptyFields() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).containsOnly(
        entry("negotiatorNameIfCompleted", Set.of("negotiatorNameIfCompleted.required")),
        entry("negotiatorEmailIfCompleted", Set.of("negotiatorEmailIfCompleted.required"))
    );
  }

  @Test
  public void validate_ValidOngoing() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    form.setNegotiatorNameIfOngoing("Name");
    form.setNegotiatorEmailIfOngoing("Email");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_ValidCompleted() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    form.setNegotiatorNameIfCompleted("Name");
    form.setNegotiatorEmailIfCompleted("Email");
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_ValidNotCrossed() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).isEmpty();
  }
}