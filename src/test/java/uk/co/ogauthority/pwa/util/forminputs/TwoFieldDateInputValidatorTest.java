package uk.co.ogauthority.pwa.util.forminputs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDate;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.AfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.BeforeDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrBeforeDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class TwoFieldDateInputValidatorTest {

  private TwoFieldDateInputValidator validator;
  private TwoFieldDateInput twoFieldDateInput;

  @Before
  public void setup() {
    validator = new TwoFieldDateInputValidator();
    twoFieldDateInput = new TwoFieldDateInput();
  }

  @Test
  public void validate_noHints_invalid_date() {
    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, new Object[0]);
    var fieldErrors = ValidatorTestUtils.extractErrors(errors);
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).containsExactly(
        entry("month", Set.of("month.invalid")),
        entry("year", Set.of("year.invalid"))
    );

    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        entry("year", Set.of("Date must be a valid date"))
    );
  }

  @Test
  public void validate_inputLabelHint_invalidDate() {
    var errors = new BeanPropertyBindingResult(twoFieldDateInput, "form");
    Object[] hints = {new FormInputLabel("Work start date")};
    ValidationUtils.invokeValidator(validator, twoFieldDateInput, errors, hints);

    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);


    assertThat(fieldErrorMessages).containsExactly(
        entry("month", Set.of("")),
        entry("year", Set.of("Work start date must be a valid date"))
    );
  }

  @Test
  public void validate_inputLabelHint_beforeDateHint_afterDateHint_invalidDate() {
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
        entry("month", Set.of("month.invalid")),
        entry("year", Set.of("year.invalid"))
    );
  }


  @Test
  public void validate_validDate() {
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
  public void validate_afterDateFail() {
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
  public void validate_beforeDateFail() {
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
  public void validate_onOrBeforeDateFail() {
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
  public void validate_onOrBeforeDatePass_sameMonth() {
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
  public void validate_onOrAfterDateFail() {
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
  public void validate_onOrAfterDatePass_sameMonth() {
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

}