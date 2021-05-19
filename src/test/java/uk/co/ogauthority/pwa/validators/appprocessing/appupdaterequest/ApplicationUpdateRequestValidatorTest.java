package uk.co.ogauthority.pwa.validators.appprocessing.appupdaterequest;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.BEFORE_TODAY;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.time.LocalDate;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.appprocessing.applicationupdate.ApplicationUpdateRequestForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.DateUtils;



@RunWith(MockitoJUnitRunner.class)
public class ApplicationUpdateRequestValidatorTest {

  private ApplicationUpdateRequestValidator validator;
  private ApplicationUpdateRequestForm form;


  @Before
  public void setUp() throws Exception {
    validator = new ApplicationUpdateRequestValidator();
    form = new ApplicationUpdateRequestForm();
  }


  @Test
  public void supports_whenValidTarget() {
    assertThat(validator.supports(ApplicationUpdateRequestForm.class)).isTrue();
  }

  @Test
  public void supports_whenInvalidTarget() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  public void validate_whenAllNull() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(
        entry("requestReason", Set.of(REQUIRED.errorCode("requestReason"))),
        entry("deadlineTimestampStr", Set.of(REQUIRED.errorCode("deadlineTimestampStr")))
    );
  }

  @Test
  public void validate_allValidProperties_noErrors() {

    form.setRequestReason("requestReason");
    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now()));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).isEmpty();
  }

  @Test
  public void validate_whenReasonProvided_tooBig() {

    form.setRequestReason(ValidatorTestUtils.over4000Chars());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(entry("requestReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("requestReason"))));
  }

  @Test
  public void validate_deadlineDateBeforeToday_error() {

    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now().minusDays(1)));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(entry("deadlineTimestampStr", Set.of(BEFORE_TODAY.errorCode("deadlineTimestampStr"))));
  }

  @Test
  public void validate_deadlineDateToday_noError() {

    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now()));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).doesNotContainKey("deadlineTimestampStr");
  }

  @Test
  public void validate_deadlineDateAfterToday_noError() {

    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now().plusDays(1)));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).doesNotContainKey("deadlineTimestampStr");
  }



}