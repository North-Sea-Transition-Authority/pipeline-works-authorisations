package uk.co.ogauthority.pwa.validators.asbuilt;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.form.asbuilt.ChangeAsBuiltNotificationGroupDeadlineForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class ChangeAsBuiltNotificationGroupDeadlineValidatorTest {

  private ChangeAsBuiltNotificationGroupDeadlineValidator changeAsBuiltNotificationGroupDeadlineValidator;

  @BeforeEach
  void setup() {
    changeAsBuiltNotificationGroupDeadlineValidator = new ChangeAsBuiltNotificationGroupDeadlineValidator();
  }

  @Test
  void validate_form_empty() {
    var form = new ChangeAsBuiltNotificationGroupDeadlineForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(changeAsBuiltNotificationGroupDeadlineValidator,
        form);

    assertThat(errorsMap).containsOnly(
        entry("newDeadlineDateTimestampStr", Set.of("newDeadlineDateTimestampStr" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_form_dateNotInFuture_validationFails() {
    var form = new ChangeAsBuiltNotificationGroupDeadlineForm();
    form.setNewDeadlineDateTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now().minusDays(1L)));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(changeAsBuiltNotificationGroupDeadlineValidator,
        form);

    assertThat(errorsMap).containsOnly(
        entry("newDeadlineDateTimestampStr",
            Set.of("newDeadlineDateTimestampStr" + FieldValidationErrorCodes.BEFORE_TODAY.getCode()))
    );
  }

  @Test
  void validate_form_validationPasses() {
    var form = new ChangeAsBuiltNotificationGroupDeadlineForm();
    form.setNewDeadlineDateTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now()));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(changeAsBuiltNotificationGroupDeadlineValidator,
        form);

    assertThat(errorsMap).isEmpty();
  }

}
