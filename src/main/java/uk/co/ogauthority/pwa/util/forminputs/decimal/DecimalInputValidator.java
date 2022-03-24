package uk.co.ogauthority.pwa.util.forminputs.decimal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.PwaNumberUtils;
import uk.co.ogauthority.pwa.util.StringDisplayUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;

@Component
public class DecimalInputValidator implements SmartValidator {

  private static final String VALUE = "value";
  private static final String VALUE_REQUIRED_CODE = VALUE + FieldValidationErrorCodes.REQUIRED.getCode();
  private static final String VALUE_INVALID_CODE = VALUE + FieldValidationErrorCodes.INVALID.getCode();
  private static final String DECIMAL_REQUIRED_ERROR_FORMAT = "Enter a number for %s";
  private static final String DECIMAL_INVALID_ERROR_FORMAT = "Enter a valid number for %s";

  static final Integer MAX_INPUT_LENGTH = 30;

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(DecimalInput.class);
  }

  @Override
  public void validate(Object o, Errors errors) {
    validate(o, errors, new Object[0]);
  }

  @Override
  public void validate(Object o, Errors errors, Object... objects) {

    // should be be small list of hints so this repeated looping over whole list is probably harmless
    var inputLabel = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(FormInputLabel.class))
        .map(hint -> ((FormInputLabel) hint))
        .findFirst()
        .orElse(new FormInputLabel("Decimal"));

    var decimalInput = (DecimalInput) o;
    var decimalOptional = decimalInput.asBigDecimal();

    boolean fieldIsMandatory = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(FieldIsOptionalHint.class))
        .map(hint -> ((FieldIsOptionalHint) hint))
        .findFirst().isEmpty();

    Optional<DecimalPlaceHint> decimalPlaceHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(DecimalPlaceHint.class))
        .map(hint -> ((DecimalPlaceHint) hint))
        .findFirst();

    Optional<PositiveNumberHint> positiveNumberHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(PositiveNumberHint.class))
        .map(hint -> ((PositiveNumberHint) hint))
        .findFirst();

    Optional<NonNegativeNumberHint> nonNegativeNumberHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(NonNegativeNumberHint.class))
        .map(hint -> ((NonNegativeNumberHint) hint))
        .findFirst();

    Optional<SmallerThanFieldHint> lessThanFieldHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(SmallerThanFieldHint.class))
        .map(hint -> ((SmallerThanFieldHint) hint))
        .findFirst();

    Optional<LessThanEqualToHint> lessThanEqualToHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(LessThanEqualToHint.class))
        .map(hint -> ((LessThanEqualToHint) hint))
        .findFirst();

    //field is null and required
    if (decimalOptional.isEmpty() && !decimalInput.hasContent() && fieldIsMandatory) {
      errors.rejectValue(
          VALUE,
          VALUE_REQUIRED_CODE,
          String.format(DECIMAL_REQUIRED_ERROR_FORMAT, inputLabel.getLabel()));

      //field required and invalid OR field optionally provided and invalid
    }  else if (decimalOptional.isEmpty() && (fieldIsMandatory || decimalInput.hasContent())) {
      errors.rejectValue(
          VALUE,
          VALUE_INVALID_CODE,
          String.format(DECIMAL_INVALID_ERROR_FORMAT, inputLabel.getLabel()));

      //field provided and valid as a big decimal type
    } else if (decimalOptional.isPresent()) {

      // only do additional validation when the decimal is valid
      decimalPlaceHint.ifPresent(hint -> validateDecimalPlaces(errors, decimalInput, inputLabel, hint));
      positiveNumberHint.ifPresent(hint -> validateAsPositiveNumber(errors, decimalInput, inputLabel));
      nonNegativeNumberHint.ifPresent(hint -> validateNonNegative(errors, decimalInput, inputLabel));
      lessThanFieldHint.ifPresent(hint -> validateLessThanField(errors, decimalInput, inputLabel, hint));
      lessThanEqualToHint.ifPresent(hint -> validateLessThanEqualToNumber(errors, decimalInput, inputLabel, hint));
      validateInputLength(errors, decimalInput, inputLabel);
    }

  }


  private void validateDecimalPlaces(Errors errors,
                                     DecimalInput decimalInput,
                                     FormInputLabel inputLabel,
                                     DecimalPlaceHint decimalPlaceHint) {

    if (PwaNumberUtils.getNumberOfDp(decimalInput.createBigDecimalOrNull()) > decimalPlaceHint.getMaxDp()) {
      var placePluralised = StringDisplayUtils.pluralise("place", decimalPlaceHint.getMaxDp());
      errors.rejectValue(VALUE, FieldValidationErrorCodes.MAX_DP_EXCEEDED.errorCode(VALUE),
          String.format("%s cannot have more than %s decimal %s",
              StringUtils.capitalize(inputLabel.getLabel()), decimalPlaceHint.getMaxDp(), placePluralised));
    }
  }

  private void validateAsPositiveNumber(Errors errors, DecimalInput decimalInput, FormInputLabel inputLabel) {

    if (decimalInput.createBigDecimalOrNull().compareTo(BigDecimal.ZERO) <= 0) {
      errors.rejectValue(VALUE, FieldValidationErrorCodes.INVALID.errorCode(VALUE),
          String.format("%s must be a positive number", StringUtils.capitalize(inputLabel.getLabel())));
    }
  }

  private void validateNonNegative(Errors errors, DecimalInput decimalInput, FormInputLabel inputLabel) {
    if (decimalInput.createBigDecimalOrNull().compareTo(BigDecimal.ZERO) < 0) {
      errors.rejectValue(VALUE,
          FieldValidationErrorCodes.INVALID.errorCode(VALUE),
          String.format("%s must have a value of 0 or greater", StringUtils.capitalize(inputLabel.getLabel())));
    }
  }


  private void validateLessThanField(Errors errors, DecimalInput decimalInput, FormInputLabel inputLabel, SmallerThanFieldHint hint) {

    if (decimalInput.createBigDecimalOrNull().compareTo(hint.getLargerNumber()) > -1) {
      errors.rejectValue(VALUE,
          FieldValidationErrorCodes.INVALID.errorCode(VALUE),
          String.format("The %s must be smaller than the %s", inputLabel.getLabel(), hint.getFormInputLabel()));
    }

  }


  private void validateLessThanEqualToNumber(Errors errors, DecimalInput decimalInput, FormInputLabel inputLabel,
                                             LessThanEqualToHint hint) {
    var value = decimalInput.createBigDecimalOrNull();
    if (value != null && hint.getLargerNumber().compareTo(value) < 0) {
      errors.rejectValue(VALUE,
          FieldValidationErrorCodes.INVALID.errorCode(VALUE),
          String.format("The %s must have a value of %s or less", inputLabel.getLabel(), hint.getLargerNumber().toPlainString()));
    }

  }

  public void validateInputLength(Errors errors, DecimalInput decimalInput, FormInputLabel inputLabel) {
    if (decimalInput.getValue().length() > MAX_INPUT_LENGTH) {
      errors.rejectValue(
          VALUE,
          FieldValidationErrorCodes.INVALID.errorCode(VALUE),
          String.format("%s must be %s characters or fewer", StringUtils.capitalize(inputLabel.getLabel()), MAX_INPUT_LENGTH)
      );
    }
  }

  public DecimalInputValidatorInvocationBuilder invocationBuilder() {
    return new DecimalInputValidatorInvocationBuilder(this);
  }

  public static class DecimalInputValidatorInvocationBuilder {

    private final List<Object> hints = new ArrayList<>();

    private final DecimalInputValidator validator;

    private DecimalInputValidatorInvocationBuilder(DecimalInputValidator validator) {
      this.validator = validator;
    }

    public DecimalInputValidatorInvocationBuilder mustHaveNoMoreThanDecimalPlaces(int maxDp) {
      this.hints.add(new DecimalPlaceHint(maxDp));
      return this;
    }

    public DecimalInputValidatorInvocationBuilder mustBeZeroOrGreater() {
      this.hints.add(new NonNegativeNumberHint());
      return this;
    }

    public DecimalInputValidatorInvocationBuilder mustBeGreaterThanZero() {
      this.hints.add(new PositiveNumberHint());
      return this;
    }

    public DecimalInputValidatorInvocationBuilder canBeOptional() {
      this.hints.add(new FieldIsOptionalHint());
      return this;
    }

    public DecimalInputValidatorInvocationBuilder mustBeLessThanOrEqualTo(BigDecimal smallerThan) {
      this.hints.add(new LessThanEqualToHint(smallerThan));
      return this;
    }

    public DecimalInputValidatorInvocationBuilder mustBeLessThanField(BigDecimal smallerThan, String label) {
      this.hints.add(new SmallerThanFieldHint(smallerThan, label));
      return this;
    }

    public void invokeNestedValidator(Errors errors, String targetPath, DecimalInput value, String label) {
      hints.add(new FormInputLabel(label));
      ValidatorUtils.invokeNestedValidator(
          errors,
          validator,
          targetPath,
          //Spring will bind an empty form field as null, but that doesn't play nicely with the validator
          DecimalInput.createEmptyIfNull(value),
          hints.toArray()
      );
    }

    public void invokeValidator(Errors errors, DecimalInput value, String label) {
      hints.add(new FormInputLabel(label));
      ValidationUtils.invokeValidator(
          validator,
          //Spring will bind an empty form field as null, but that doesn't play nicely with the validator
          DecimalInput.createEmptyIfNull(value),
          errors,
          hints.toArray()
      );
    }

  }

}
