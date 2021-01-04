package uk.co.ogauthority.pwa.validators.appprocessing.options;


import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.BEFORE_SOME_DATE;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.appprocessing.options.ChangeOptionsApprovalDeadlineForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ChangeOptionsApprovalDeadlineFormValidatorTest {

  private static final String DAY_ATTR = "deadlineDateDay";
  private static final String MONTH_ATTR = "deadlineDateMonth";
  private static final String YEAR_ATTR = "deadlineDateYear";
  private static final String NOTE_ATTR = "note";

  private ChangeOptionsApprovalDeadlineFormValidator changeOptionsApprovalDeadlineFormValidator;

  private ChangeOptionsApprovalDeadlineForm form;

  @Before
  public void setUp() throws Exception {
    changeOptionsApprovalDeadlineFormValidator = new ChangeOptionsApprovalDeadlineFormValidator();
    form = new ChangeOptionsApprovalDeadlineForm();
  }

  @Test
  public void supports_whenSupported() {
    assertThat(changeOptionsApprovalDeadlineFormValidator.supports(ChangeOptionsApprovalDeadlineForm.class)).isTrue();
  }

  @Test
  public void supports_whenNotSupported() {
    assertThat(changeOptionsApprovalDeadlineFormValidator.supports(Object.class)).isFalse();
  }

  @Test
  public void validate_whenAllNull() {
    var results = ValidatorTestUtils.getFormValidationErrors(changeOptionsApprovalDeadlineFormValidator, form);

    assertThat(results).containsOnly(
        Map.entry(DAY_ATTR, Set.of(REQUIRED.errorCode(DAY_ATTR))),
        Map.entry(MONTH_ATTR, Set.of(REQUIRED.errorCode(MONTH_ATTR))),
        Map.entry(YEAR_ATTR, Set.of(REQUIRED.errorCode(YEAR_ATTR))),
        Map.entry(NOTE_ATTR, Set.of(REQUIRED.errorCode(NOTE_ATTR)))
    );

  }

  @Test
  public void validate_whenDateInPast() {

    form.setDeadlineDateDay(1);
    form.setDeadlineDateMonth(1);
    form.setDeadlineDateYear(1993);
    form.setNote("some note");

    var results = ValidatorTestUtils.getFormValidationErrors(changeOptionsApprovalDeadlineFormValidator, form);

    assertThat(results).containsOnly(
        Map.entry(DAY_ATTR, Set.of(BEFORE_SOME_DATE.errorCode(DAY_ATTR))),
        Map.entry(MONTH_ATTR, Set.of(BEFORE_SOME_DATE.errorCode(MONTH_ATTR))),
        Map.entry(YEAR_ATTR, Set.of(BEFORE_SOME_DATE.errorCode(YEAR_ATTR)))
    );

  }

  @Test
  public void validate_whenNoteTooLong() {

    form.setNote(ValidatorTestUtils.over4000Chars());

    var results = ValidatorTestUtils.getFormValidationErrors(changeOptionsApprovalDeadlineFormValidator, form);

    assertThat(results).contains(
        Map.entry(NOTE_ATTR, Set.of(MAX_LENGTH_EXCEEDED.errorCode(NOTE_ATTR)))
    );

  }
}