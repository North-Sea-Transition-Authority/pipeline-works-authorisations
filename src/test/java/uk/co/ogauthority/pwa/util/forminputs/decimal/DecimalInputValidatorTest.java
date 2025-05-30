package uk.co.ogauthority.pwa.util.forminputs.decimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;

@ExtendWith(MockitoExtension.class)
class DecimalInputValidatorTest {

  @Spy
  private DecimalInputValidator validator;
  private DecimalInput decimalInput;

  private static final String VALUE = "value";
  private static final String VALUE_REQUIRED_CODE = VALUE + FieldValidationErrorCodes.REQUIRED.getCode();
  private static final String VALUE_INVALID_CODE = VALUE + FieldValidationErrorCodes.INVALID.getCode();

  @BeforeEach
  void setup() {
    decimalInput = new DecimalInput();
  }

  private Map<String, Set<String>> getValidationErrors() {
    return getValidationErrors(List.of(new Object[0]));
  }

  private Map<String, Set<String>> getValidationErrors(List<Object> validationHints) {
    var errors = new BeanPropertyBindingResult(decimalInput, "form");
    return getValidationErrors(errors, validationHints);
  }
  private Map<String, Set<String>> getValidationErrors(BindingResult errors, List<Object> validationHints) {
    ValidationUtils.invokeValidator(validator, decimalInput, errors, validationHints.toArray());
    return ValidatorTestUtils.extractErrors(errors);
  }

  private void assertSingleErrorMessageContains(Map<String, Set<String>> fieldErrorMessages, String messageContent) {
    assertThat(fieldErrorMessages).hasSize(1);
    assertThat(fieldErrorMessages.get(VALUE)).hasSize(1);
    assertThat(fieldErrorMessages.get(VALUE).iterator().next()).contains(messageContent);
  }

  //Value required and validity tests
  @Test
  void validate_nullValue_fieldRequired_error() {
    var fieldErrors = getValidationErrors();
    assertThat(fieldErrors).containsExactly(
        entry(VALUE, Set.of(VALUE_REQUIRED_CODE))
    );
  }

  @Test
  void validate_fieldRequired_blankValue_error() {

    decimalInput.setValue("");
    var fieldErrors = getValidationErrors();

    assertThat(fieldErrors).containsExactly(
        entry(VALUE, Set.of(VALUE_REQUIRED_CODE))
    );
  }

  @Test
  void validate_fieldRequired_invalidValue_error() {

    decimalInput.setValue("invalid num");
    var fieldErrors = getValidationErrors();

    assertThat(fieldErrors).containsExactly(
        entry(VALUE, Set.of(VALUE_INVALID_CODE))
    );
  }

  @Test
  void validate_fieldRequired_validValueProvided_noError() {

    decimalInput.setValue("1.1");
    var fieldErrors = getValidationErrors();

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE_REQUIRED_CODE)),
        entry(VALUE, Set.of(VALUE_INVALID_CODE))
    );
  }

  @Test
  void validate_fieldOptional_invalidValue_error() {

    decimalInput.setValue("invalid num");
    var fieldErrors = getValidationErrors(List.of(new FieldIsOptionalHint()));

    assertThat(fieldErrors).containsExactly(
        entry(VALUE, Set.of(VALUE_INVALID_CODE))
    );
  }

  @Test
  void validate_fieldOptional_noValueProvided_noError() {

    var fieldErrors = getValidationErrors(List.of(new FieldIsOptionalHint()));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE_REQUIRED_CODE)),
        entry(VALUE, Set.of(VALUE_INVALID_CODE))
    );
  }

  @Test
  void validate_fieldOptional_blankValueProvided_noError() {

    decimalInput.setValue("");
    var fieldErrors = getValidationErrors(List.of(new FieldIsOptionalHint()));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE_REQUIRED_CODE)),
        entry(VALUE, Set.of(VALUE_INVALID_CODE))
    );
  }

  @Test
  void validate_fieldOptional_validValueProvided_noError() {

    decimalInput.setValue("1.1");
    var fieldErrors = getValidationErrors(List.of(new FieldIsOptionalHint()));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE_REQUIRED_CODE)),
        entry(VALUE, Set.of(VALUE_INVALID_CODE))
    );
  }


  //Decimal Place Hint tests
  @Test
  void validate_validBigDecimal_decimalPlaceHintProvided_valueDpExceedsMaxDp_error() {

    decimalInput.setValue("1.222");
    var errors = new BeanPropertyBindingResult(decimalInput, "form");
    var fieldErrors = getValidationErrors(errors, List.of(new DecimalPlaceHint(2)));
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).contains(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.MAX_DP_EXCEEDED.getCode()))
    );

    assertSingleErrorMessageContains(fieldErrorMessages, "cannot have more than");
  }

  @Test
  void validate_validBigDecimal_decimalPlaceHintProvided_valueDpEqualsMaxDp_noError() {

    decimalInput.setValue("1.22");
    var fieldErrors = getValidationErrors(List.of(new DecimalPlaceHint(2)));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.MAX_DP_EXCEEDED.getCode()))
    );
  }

  @Test
  void validate_validBigDecimal_decimalPlaceHintProvided_valueActualDpEqualsMaxDp_trailingZeroExceedMaxDp_noError() {

    decimalInput.setValue("1.220");
    var fieldErrors = getValidationErrors(List.of(new DecimalPlaceHint(2)));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.MAX_DP_EXCEEDED.getCode()))
    );
  }

  @Test
  void validate_validBigDecimal_decimalPlaceHintProvided_valueDpLessThanMaxDp_noError() {

    decimalInput.setValue("1.2");
    var fieldErrors = getValidationErrors(List.of(new DecimalPlaceHint(2)));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.MAX_DP_EXCEEDED.getCode()))
    );
  }


  //Positive Number Hint tests
  @Test
  void validate_validBigDecimal_positiveNumberHintProvided_negativeValue_error() {

    decimalInput.setValue("-1");

    var errors = new BeanPropertyBindingResult(decimalInput, "form");
    var fieldErrors = getValidationErrors(errors, List.of(new PositiveNumberHint()));
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).contains(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );

    assertSingleErrorMessageContains(fieldErrorMessages, "Decimal must be more than 0");
  }

  @Test
  void validate_validBigDecimal_positiveNumberHintProvided_0Value_error() {

    decimalInput.setValue("0");

    var fieldErrors = getValidationErrors(List.of(new PositiveNumberHint()));

    assertThat(fieldErrors).contains(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_validBigDecimal_positiveNumberHintProvided_positiveValue_noError() {

    decimalInput.setValue("1");

    var fieldErrors = getValidationErrors(List.of(new PositiveNumberHint()));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }


  //Non Negative Number Hint tests
  @Test
  void validate_validBigDecimal_nonNegativeNumberHintProvided_negativeValue_error() {

    decimalInput.setValue("-1");

    var errors = new BeanPropertyBindingResult(decimalInput, "form");
    var fieldErrors = getValidationErrors(errors, List.of(new NonNegativeNumberHint()));
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).contains(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );

    assertSingleErrorMessageContains(fieldErrorMessages, "must be 0 or more");
  }

  @Test
  void validate_validBigDecimal_nonNegativeNumberHintProvided_0Value_noError() {

    decimalInput.setValue("0");

    var fieldErrors = getValidationErrors(List.of(new NonNegativeNumberHint()));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_validBigDecimal_nonNegativeNumberHintProvided_positiveValue_noError() {

    decimalInput.setValue("1");

    var fieldErrors = getValidationErrors(List.of(new NonNegativeNumberHint()));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }


  //Smaller Than Field Hint tests
  @Test
  void validate_validBigDecimal_smallerThanFieldHintProvided_valueLargerThanMaxAllowed_error() {

    var smallerThanNumberHint = new SmallerThanFieldHint(new BigDecimal("5"), "My label");
    decimalInput.setValue("6");

    var errors = new BeanPropertyBindingResult(decimalInput, "form");
    var fieldErrors = getValidationErrors(errors, List.of(smallerThanNumberHint));
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).contains(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );

    assertSingleErrorMessageContains(fieldErrorMessages, "must be smaller than");
  }

  @Test
  void validate_validBigDecimal_smallerThanFieldHintProvided_valueEqualsMaxAllowed_error() {

    var smallerThanNumberHint = new SmallerThanFieldHint(new BigDecimal("5"), "My label");
    decimalInput.setValue("5");

    var fieldErrors = getValidationErrors(List.of(smallerThanNumberHint));

    assertThat(fieldErrors).contains(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_validBigDecimal_smallerThanFieldHintProvided_valueLessThanMaxAllowed_noError() {

    var smallerThanNumberHint = new SmallerThanFieldHint(new BigDecimal("5"), "My label");
    decimalInput.setValue("4");

    var fieldErrors = getValidationErrors(List.of(smallerThanNumberHint));

    assertThat(fieldErrors).doesNotContain(
        entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  //Smaller Than Number Hint Hint tests
  @Test
  void validate_validBigDecimal_smallerThanNumberHintProvided_valueLargerThanMaxAllowed_error() {

    var smallerThanNumberHint = new LessThanEqualToHint(new BigDecimal("5"));
    decimalInput.setValue("6");

    var errors = new BeanPropertyBindingResult(decimalInput, "form");
    var fieldErrors = getValidationErrors(errors, List.of(smallerThanNumberHint));
    var fieldErrorMessages = ValidatorTestUtils.extractErrorMessages(errors);

    assertThat(fieldErrors).contains(
      entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );

    assertSingleErrorMessageContains(fieldErrorMessages, "5 or less");
  }

  @Test
  void validate_validBigDecimal_smallerThanNumberHintProvided_valueEqualsMaxAllowed_error() {

    var smallerThanNumberHint = new LessThanEqualToHint(new BigDecimal("5"));
    decimalInput.setValue("6");

    var fieldErrors = getValidationErrors(List.of(smallerThanNumberHint));

    assertThat(fieldErrors).contains(
      entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_validBigDecimal_smallerThanNumberHintProvided_valueLessThanMaxAllowed_noError() {

    var smallerThanNumberHint = new LessThanEqualToHint(new BigDecimal("5"));
    decimalInput.setValue("4");

    var fieldErrors = getValidationErrors(List.of(smallerThanNumberHint));

    assertThat(fieldErrors).doesNotContain(
      entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_validBigDecimal_equalToNumberHintProvided_valueLessThanMaxAllowed_noError() {

    var smallerThanNumberHint = new LessThanEqualToHint(new BigDecimal("5"));
    decimalInput.setValue("5");

    var fieldErrors = getValidationErrors(List.of(smallerThanNumberHint));

    assertThat(fieldErrors).doesNotContain(
      entry(VALUE, Set.of(VALUE + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  void validate_validBigDecimal_exponentsPrevented() {

    var errorsExpectedToValuesMap = Map.of(
        true, List.of("1.23E3", "1.23E+3", "12.3E+7", "-1.23E-12", "1234.5E-4", "0E+7", "0e+7"),
        false, List.of("0", "0.00", "123", "-123", "12.0", "12.3", "0.00123", "-0")
    );

    errorsExpectedToValuesMap.forEach((errorsExpected, values) ->

      values.forEach(value -> {

        decimalInput = new DecimalInput(value);

        var fieldErrors = getValidationErrors();

        if (errorsExpected) {
          assertThat(fieldErrors).contains(entry(VALUE, Set.of(VALUE_INVALID_CODE)));
        } else {
          assertThat(fieldErrors).isEmpty();
        }

      }));

  }

  @Test
  void validate_valueEnteredHasTooManyDigits_invalidError() {

    var inputWithMoreDigitsThanAllowed = "9".repeat(DecimalInputValidator.MAX_INPUT_LENGTH + 1);
    decimalInput.setValue(inputWithMoreDigitsThanAllowed);
    var fieldErrors = getValidationErrors();

    assertThat(fieldErrors).contains(
      entry(VALUE, Set.of(VALUE_INVALID_CODE))
    );
  }

  @Test
  void validate_valueEnteredHasMaxDigits_noErrors() {

    var inputWithNumberOfDigitsAllowed  = "9".repeat(DecimalInputValidator.MAX_INPUT_LENGTH);
    decimalInput.setValue(inputWithNumberOfDigitsAllowed );
    var fieldErrors = getValidationErrors();

    assertThat(fieldErrors).doesNotContain(
      entry(VALUE, Set.of(VALUE_INVALID_CODE))
    );
  }

  @Test
  void validate_partialValidation_allowsEmptyInput() {
  decimalInput.setValue("");
  var fieldErrors = getValidationErrors(List.of(new PartialValidateHint(), new PositiveNumberHint()));

  assertThat(fieldErrors).isEmpty();
  }

  @Test
  void validate_partialValidation_validatesLength() {
    decimalInput.setValue("-" + "9".repeat(DecimalInputValidator.MAX_INPUT_LENGTH));
    var fieldErrors = getValidationErrors(List.of(new PartialValidateHint()));

    assertThat(fieldErrors).contains(
        entry(VALUE, Set.of(VALUE_INVALID_CODE))
      );
  }

  @Test
  void validate_partialValidation_validatesHintsPassedIn() {
    decimalInput.setValue("-35");
    var fieldErrors = getValidationErrors(List.of(new PartialValidateHint(), new PositiveNumberHint()));

    assertThat(fieldErrors).contains(
      entry(VALUE, Set.of(VALUE_INVALID_CODE))
    );
  }

  @Test
  void builderAllFunctions() {

    var errors = new BeanPropertyBindingResult(decimalInput, "form");

    var LABEL = "testLabel";

    validator.invocationBuilder()
      .canBeOptional()
      .mustHaveNoMoreThanDecimalPlaces(2)
      .mustBeLessThanOrEqualTo(BigDecimal.TEN)
      .mustBeGreaterThanZero()
      .mustBeZeroOrGreater()
      .invokeValidator(errors, decimalInput, LABEL);

    var labelArgCaptor = ArgumentCaptor.forClass(FormInputLabel.class);

    verify(validator).validate(eq(decimalInput), eq(errors),
      any(FieldIsOptionalHint.class),
      eq(new DecimalPlaceHint(2)),
      eq(new LessThanEqualToHint(BigDecimal.TEN)),
      any(PositiveNumberHint.class),
      any(NonNegativeNumberHint.class),
      labelArgCaptor.capture());

    assertThat(labelArgCaptor.getValue())
      .extracting(FormInputLabel::getLabel)
      .isEqualTo(LABEL);
  }

  @Test
  void builderOneFunction() {

    var errors = new BeanPropertyBindingResult(decimalInput, "form");

    var LABEL = "testLabel";

    validator.invocationBuilder()
      .mustBeLessThanOrEqualTo(BigDecimal.TEN)
      .invokeValidator(errors, decimalInput, LABEL);

    var labelArgCaptor = ArgumentCaptor.forClass(FormInputLabel.class);

    verify(validator).validate(eq(decimalInput), eq(errors),
      eq(new LessThanEqualToHint(BigDecimal.TEN)),
      labelArgCaptor.capture());

    assertThat(labelArgCaptor.getValue())
      .extracting(FormInputLabel::getLabel)
      .isEqualTo(LABEL);
  }

  @Test
  void nullDecimalInput_optionalField() {

    DecimalInputForm decimalInputForm = new DecimalInputForm();
    var errors = new BeanPropertyBindingResult(decimalInputForm, "form");

    validator.invocationBuilder()
      .canBeOptional()
      .invokeNestedValidator(errors, "decimalInput", decimalInputForm.decimalInput, "nullLabel");

    var errorMap = ValidatorTestUtils.extractErrors(errors);
    assertThat(errorMap).isEmpty();

  }

  @Test
  void nullDecimalInput_notOptional() {

    DecimalInputForm decimalInputForm = new DecimalInputForm();
    var errors = new BeanPropertyBindingResult(decimalInputForm, "form");

    validator.invocationBuilder()
      .invokeNestedValidator(errors, "decimalInput", decimalInputForm.decimalInput, "nullLabel");

    var errorMap = ValidatorTestUtils.extractErrors(errors);
    assertThat(errorMap).containsEntry("decimalInput.value", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("value")));

  }

  /**
   * To properly test the null vlaues in the form, we need to wrap the DecimalInput in a form.
   * This class is a mock form containing a decimal input for this purpose
   */
  private static class DecimalInputForm {

    DecimalInput decimalInput;

    public DecimalInput getDecimalInput() {
      return decimalInput;
    }

    public void setDecimalInput(DecimalInput decimalInput) {
      this.decimalInput = decimalInput;
    }
  }

}