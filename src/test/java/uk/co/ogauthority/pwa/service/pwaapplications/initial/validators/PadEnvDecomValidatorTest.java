package uk.co.ogauthority.pwa.service.pwaapplications.initial.validators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.EnvDecomForm;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

public class PadEnvDecomValidatorTest {

  private PadEnvDecomValidator validator;

  @Before
  public void setUp() {
    validator = new PadEnvDecomValidator();
  }

  @Test
  public void emptyDate() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("emtSubmissionDay")).containsExactly("NO_CONTENT");
    assertThat(errors.get("emtSubmissionMonth")).containsExactly("NO_CONTENT");
    assertThat(errors.get("emtSubmissionYear")).containsExactly("NO_CONTENT");
  }

  @Test
  public void invalidDate() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    form.setEmtSubmissionDay(-1);
    form.setEmtSubmissionMonth(-1);
    form.setEmtSubmissionYear(-1);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("emtSubmissionDay")).containsExactly("INVALID_DATE");
    assertThat(errors.get("emtSubmissionMonth")).containsExactly("INVALID_DATE");
    assertThat(errors.get("emtSubmissionYear")).containsExactly("INVALID_DATE");
  }

  @Test
  public void validDate() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    form.setEmtSubmissionDay(16);
    form.setEmtSubmissionMonth(3);
    form.setEmtSubmissionYear(2020);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("emtSubmissionDay")).isNull();
    assertThat(errors.get("emtSubmissionMonth")).isNull();
    assertThat(errors.get("emtSubmissionYear")).isNull();
  }

  @Test
  public void nullPermitsSubmittedField() {
    var form = new EnvDecomForm();
    form.setEmtHasSubmittedPermits(true);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsSubmitted")).containsExactly("NO_CONTENT");
  }

  @Test
  public void emptyPermitsSubmittedField() {
    var form = new EnvDecomForm();
    form.setEmtHasSubmittedPermits(true);
    form.setPermitsSubmitted("");
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsSubmitted")).containsExactly("NO_CONTENT");
  }

  @Test
  public void validPermitsSubmittedField() {
    var form = new EnvDecomForm();
    form.setEmtHasSubmittedPermits(true);
    form.setPermitsSubmitted("Test");
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsSubmitted")).isNull();
  }

  @Test
  public void nullPermitsPendingSubmissionField() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsPendingSubmission")).containsExactly("NO_CONTENT");
  }

  @Test
  public void emptyPermitsPendingSubmissionField() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    form.setPermitsPendingSubmission("");
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsPendingSubmission")).containsExactly("NO_CONTENT");
  }

  @Test
  public void validPermitsPendingSubmissionField() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    form.setPermitsPendingSubmission("Test");
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsPendingSubmission")).isNull();
  }
}