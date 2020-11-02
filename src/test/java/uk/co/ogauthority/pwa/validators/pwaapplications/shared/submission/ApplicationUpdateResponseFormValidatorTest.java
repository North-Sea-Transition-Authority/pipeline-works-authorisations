package uk.co.ogauthority.pwa.validators.pwaapplications.shared.submission;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.submission.ApplicationUpdateResponseForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationUpdateResponseFormValidatorTest {

  private ApplicationUpdateResponseFormValidator validator;

  private ApplicationUpdateResponseForm form;
  @Before
  public void setUp() throws Exception {
    validator = new ApplicationUpdateResponseFormValidator();
    form = new ApplicationUpdateResponseForm();
  }

  @Test
  public void validate_whenAllNull() {
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).containsExactly(
        entry("madeOnlyRequestedChanges", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("madeOnlyRequestedChanges")))
    );

  }


  @Test
  public void validate_whenExtraChanges_noDesc() {
    form.setMadeOnlyRequestedChanges(false);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).containsExactly(
        entry("otherChangesDescription", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("otherChangesDescription")))
    );

  }

  @Test
  public void validate_whenExtraChanges_tooLong() {

    form.setMadeOnlyRequestedChanges(false);
    form.setOtherChangesDescription(ValidatorTestUtils.over4000Chars());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).containsExactly(
        entry("otherChangesDescription", Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("otherChangesDescription")))
    );

  }

  @Test
  public void validate_wonlyRequestedChanges() {

    form.setMadeOnlyRequestedChanges(true);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).isEmpty();

  }
}