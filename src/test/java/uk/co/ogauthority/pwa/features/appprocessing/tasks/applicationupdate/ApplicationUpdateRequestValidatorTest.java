package uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.BEFORE_TODAY;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.DateUtils;


@ExtendWith(MockitoExtension.class)
class ApplicationUpdateRequestValidatorTest {

  private ApplicationUpdateRequestValidator validator;
  private ApplicationUpdateRequestForm form;


  @BeforeEach
  void setUp() throws Exception {
    validator = new ApplicationUpdateRequestValidator();
    form = new ApplicationUpdateRequestForm();
  }


  @Test
  void supports_whenValidTarget() {
    assertThat(validator.supports(ApplicationUpdateRequestForm.class)).isTrue();
  }

  @Test
  void supports_whenInvalidTarget() {
    assertThat(validator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenAllNull() {
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(
        entry("requestReason", Set.of(REQUIRED.errorCode("requestReason"))),
        entry("deadlineTimestampStr", Set.of(REQUIRED.errorCode("deadlineTimestampStr")))
    );
  }

  @Test
  void validate_allValidProperties_noErrors() {

    form.setRequestReason("requestReason");
    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now()));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).isEmpty();
  }

  @Test
  void validate_whenReasonProvided_tooBig() {

    form.setRequestReason(ValidatorTestUtils.overMaxDefaultCharLength());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).doesNotContain(entry("requestReason", Set.of(MAX_LENGTH_EXCEEDED.errorCode("requestReason"))));
  }

  @Test
  void validate_deadlineDateBeforeToday_error() {

    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now().minusDays(1)));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).contains(entry("deadlineTimestampStr", Set.of(BEFORE_TODAY.errorCode("deadlineTimestampStr"))));
  }

  @Test
  void validate_deadlineDateToday_noError() {

    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now()));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).doesNotContainKey("deadlineTimestampStr");
  }

  @Test
  void validate_deadlineDateAfterToday_noError() {

    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now().plusDays(1)));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(result).doesNotContainKey("deadlineTimestampStr");
  }



}