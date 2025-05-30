package uk.co.ogauthority.pwa.util.forminputs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.AfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.BeforeDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.DateWithinRangeHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrBeforeDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@ExtendWith(MockitoExtension.class)
class TwoFieldDateInputValidatorTest {

  private TwoFieldDateInputValidator validator;
  private TwoFieldDateInput twoFieldDateInput;

  @BeforeEach
  void setup() {
    validator = new TwoFieldDateInputValidator();
    twoFieldDateInput = new TwoFieldDateInput();
  }

  @Test
  void validate_noHints_emptyDateValues() {
    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, new Object[0]);
    var fieldErrors = ValidatorTestUtils.extractErrors(errors);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).containsExactly(
        entry("month", Set.of("month.required")),
        entry("year", Set.of("year.required"))
    );

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        entry("year", Set.of("Enter a Date"))
    );
  }

  @Test
  void validate_inputLabelHint_invalidDate() {
    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {new FormInputLabel("Work start")};
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);


    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        entry("year", Set.of("Enter a Work start date"))
    );
  }

  @Test
  void validate_inputLabelHint_beforeDateHint_afterDateHint_emptyDate() {
    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("Some date"),
        new BeforeDateHint(LocalDate.of(2020, 12, 31), "Before date"),
        new AfterDateHint(LocalDate.of(2020, 1, 1), "After date")

    };

    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);

    // as date is invalid do not do additional validation
    assertThat(fieldErrors).containsExactly(
        entry("month", Set.of("month.required")),
        entry("year", Set.of("year.required"))
    );
  }


  @Test
  void validate_validDate() {
    twoFieldDateInput.setMonth(6);
    twoFieldDateInput.setYear(2020);

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("Some date"),
        new BeforeDateHint(LocalDate.of(2020, 12, 31), "Before date"),
        new AfterDateHint(LocalDate.of(2020, 1, 1), "After date")

    };

    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);

    assertThat(fieldErrors).isEmpty();
  }

  @Test
  void validate_afterDateFail() {
    twoFieldDateInput.setMonth(12);
    twoFieldDateInput.setYear(2019);

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("Some date"),
        new BeforeDateHint(LocalDate.of(2020, 12, 31), "Before date"),
        new AfterDateHint(LocalDate.of(2020, 1, 1), "After date")

    };
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).containsExactly(
        entry("month", Set.of("month.afterDate")),
        entry("year", Set.of("year.afterDate"))
    );

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        // case preserved. up to callers to make sure they provide a good hint
        entry("year", Set.of("Some date must be after After date"))
    );
  }

  @Test
  void validate_beforeDateFail() {
    twoFieldDateInput.setMonth(12);
    twoFieldDateInput.setYear(2021);

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("Some date"),
        new BeforeDateHint(LocalDate.of(2020, 12, 31), "Before date"),
        new AfterDateHint(LocalDate.of(2020, 1, 1), "After date")

    };
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).containsExactly(
        entry("month", Set.of("month.beforeDate")),
        entry("year", Set.of("year.beforeDate"))
    );

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        // case preserved. up to callers to make sure they provide a good hint
        entry("year", Set.of("Some date must be before Before date"))
    );
  }

  @Test
  void validate_onOrBeforeDateFail() {
    twoFieldDateInput.setMonth(12);
    twoFieldDateInput.setYear(2021);

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("Some date"),
        new OnOrBeforeDateHint(LocalDate.of(2020, 12, 31), "On/Before date"),

    };
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).containsExactly(
        entry("month", Set.of("month.beforeDate")),
        entry("year", Set.of("year.beforeDate"))
    );

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        // case preserved. up to callers to make sure they provide a good hint
        entry("year", Set.of("Some date must be the same as or before On/Before date"))
    );
  }

  @Test
  void validate_onOrBeforeDatePass_sameMonth() {
    twoFieldDateInput.setMonth(12);
    twoFieldDateInput.setYear(2020);

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("Some date"),
        new OnOrBeforeDateHint(LocalDate.of(2020, 12, 31), "On/Before date"),

    };
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);

    assertThat(fieldErrors).isEmpty();

  }

  @Test
  void validate_onOrAfterDateFail() {
    twoFieldDateInput.setMonth(12);
    twoFieldDateInput.setYear(2020);

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("Some date"),
        new OnOrAfterDateHint(LocalDate.of(2021, 12, 31), "On/After date"),

    };
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).containsExactly(
        entry("month", Set.of("month.afterDate")),
        entry("year", Set.of("year.afterDate"))
    );

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        // case preserved. up to callers to make sure they provide a good hint
        entry("year", Set.of("Some date must be the same as or after On/After date"))
    );
  }

  @Test
  void validate_onOrAfterDatePass_sameMonth() {
    twoFieldDateInput.setMonth(12);
    twoFieldDateInput.setYear(2020);

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("Some date"),
        new OnOrAfterDateHint(LocalDate.of(2020, 12, 31), "On/After date"),

    };
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).isEmpty();
  }


  @Test
  void validate_dateWithinRange_withinRange() {

    var fromDate = LocalDate.now();
    var toDate = fromDate.plusMonths(12);
    twoFieldDateInput = new TwoFieldDateInput(fromDate.plusMonths(10));

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("toDate"),
        new DateWithinRangeHint(fromDate, toDate,"")
    };
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);

    assertThat(fieldErrors).isEmpty();
  }

  @Test
  void validate_dateWithinRange_pastMaxRange() {

    var maxMonthRange = 12;
    var fromDate = LocalDate.now();
    var toDate = fromDate.plusMonths(maxMonthRange);
    twoFieldDateInput = new TwoFieldDateInput(fromDate.plusMonths(13));

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("toDate"),
        new DateWithinRangeHint(fromDate, toDate,maxMonthRange + " months of the deposit start date")
    };
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).containsExactly(
        entry("month", Set.of("month" + FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.getCode())),
        entry("year", Set.of("year" + FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.getCode()))
    );

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        entry("year", Set.of("toDate must be within the range of 12 months of the deposit start date")));
  }

  @Test
  void validate_dateWithinRange_beforeFromDate() {

    var maxMonthRange = 12;
    var fromDate = LocalDate.now();
    var toDate = fromDate.plusMonths(maxMonthRange);
    twoFieldDateInput = new TwoFieldDateInput(fromDate.minusMonths(1));

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {
        new FormInputLabel("toDate"),
        new DateWithinRangeHint(fromDate, toDate,maxMonthRange + " months of the deposit start date")
    };
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrors = ValidatorTestUtils.extractErrors(errors);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).containsExactly(
        entry("month", Set.of("month" + FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.getCode())),
        entry("year", Set.of("year" + FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.getCode()))
    );

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        entry("year", Set.of("toDate must be within the range of 12 months of the deposit start date")));
  }


  @Test
  void dateRequired_formatValidationMessage_messageContainsDateTextTwiceAdjacent_formattedToOnly1DateText() {

    Object[] hints = {new FormInputLabel("Deposit start date")};
    twoFieldDateInput = new TwoFieldDateInput();

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        entry("year", Set.of("Enter a Deposit start date"))
    );
  }


  @Test
  void dateRequired_formatValidationMessage_messageDoesNotContainDateTextTwiceAdjacent_noFormattingApplied() {

    Object[] hints = {new FormInputLabel("Deposit start")};
    twoFieldDateInput = new TwoFieldDateInput();

    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        entry("year", Set.of("Enter a Deposit start date"))
    );
  }



}