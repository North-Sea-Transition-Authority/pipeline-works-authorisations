package uk.co.ogauthority.pwa.service.pwaapplications.validators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.EnvDecomForm;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.PadEnvDecomValidator;

public class PadEnvDecomValidatorTest {

  private PadEnvDecomValidator validator;

  @Before
  public void setUp() {
    validator = new PadEnvDecomValidator();
  }

  @Test
  public void testValidate_EmptyDate() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("emtSubmissionDay")).containsExactly("emtSubmissionDay.notParsable");
    assertThat(errors.get("emtSubmissionMonth")).containsExactly("emtSubmissionMonth.notParsable");
    assertThat(errors.get("emtSubmissionYear")).containsExactly("emtSubmissionYear.notParsable");
  }

  @Test
  public void testValidate_InvalidDate() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    form.setEmtSubmissionDay(-1);
    form.setEmtSubmissionMonth(-1);
    form.setEmtSubmissionYear(-1);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("emtSubmissionDay")).containsExactly("emtSubmissionDay.invalidDate");
    assertThat(errors.get("emtSubmissionMonth")).containsExactly("emtSubmissionMonth.invalidDate");
    assertThat(errors.get("emtSubmissionYear")).containsExactly("emtSubmissionYear.invalidDate");
  }

  @Test
  public void testValidate_ValidDate() {
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
  public void testValidate_NullPermitsSubmittedField() {
    var form = new EnvDecomForm();
    form.setEmtHasSubmittedPermits(true);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsSubmitted")).containsExactly("permitsSubmitted.empty");
  }

  @Test
  public void testValidate_EmptyPermitsSubmittedField() {
    var form = new EnvDecomForm();
    form.setEmtHasSubmittedPermits(true);
    form.setPermitsSubmitted("");
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsSubmitted")).containsExactly("permitsSubmitted.empty");
  }

  @Test
  public void testValidate_ValidPermitsSubmittedField() {
    var form = new EnvDecomForm();
    form.setEmtHasSubmittedPermits(true);
    form.setPermitsSubmitted("Test");
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsSubmitted")).isNull();
  }

  @Test
  public void testValidate_NullPermitsPendingSubmissionField() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsPendingSubmission")).containsExactly("permitsPendingSubmission.empty");
  }

  @Test
  public void testValidate_EmptyPermitsPendingSubmissionField() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    form.setPermitsPendingSubmission("");
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsPendingSubmission")).containsExactly("permitsPendingSubmission.empty");
  }

  @Test
  public void testValidate_ValidPermitsPendingSubmissionField() {
    var form = new EnvDecomForm();
    form.setEmtHasOutstandingPermits(true);
    form.setPermitsPendingSubmission("Test");
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors.get("permitsPendingSubmission")).isNull();
  }
}