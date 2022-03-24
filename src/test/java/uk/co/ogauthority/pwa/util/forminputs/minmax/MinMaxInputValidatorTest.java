package uk.co.ogauthority.pwa.util.forminputs.minmax;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.enums.validation.MinMaxValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class MinMaxInputValidatorTest {

  private MinMaxInputValidator validator;

  @Before
  public void setup() {
    validator = new MinMaxInputValidator();
  }

  @Test
  public void validate_notEmpty() {
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, new MinMaxInput(), "My Property", List.of(), List.of());

    assertThat(errorsMap).contains(
        Map.entry("minValue", Set.of("minValue" + FieldValidationErrorCodes.REQUIRED.getCode())),
        Map.entry("maxValue", Set.of("maxValue" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_minSmallerOrEqualToMax() {
    var validationRequiredHints = List.of();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, new MinMaxInput(String.valueOf(5), String.valueOf(4)), "My Property", List.of(), validationRequiredHints);

    assertThat(errorsMap).contains(
        Map.entry("minValue", Set.of("minValue" + MinMaxValidationErrorCodes.MIN_LARGER_THAN_MAX.getCode()))
    );
  }


  @Test
  public void validate_minSmallerOrEqualToMax_noRestriction() {
    var validationRequiredHints = List.of();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, new MinMaxInput(String.valueOf(5), String.valueOf(4)), "My Property", List.of(DefaultValidationRule.MIN_SMALLER_THAN_MAX), validationRequiredHints);

    assertThat(errorsMap).doesNotContain(
        Map.entry("maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.MIN_LARGER_THAN_MAX.getCode()))
    );
  }

  @Test
  public void validate_inputSizeLargerThanMax() {
    var validationRequiredHints = List.of();
    var inputWithNumberOfDigitsAllowed = "9".repeat(MinMaxInputValidator.MAX_INPUT_LENGTH);
    var inputWithMoreDigitsThanAllowed = "9".repeat(MinMaxInputValidator.MAX_INPUT_LENGTH + 1);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(
      validator, new MinMaxInput(inputWithNumberOfDigitsAllowed, inputWithMoreDigitsThanAllowed), "My Property", List.of(), validationRequiredHints);

    assertThat(errorsMap).contains(
      Map.entry("maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    ).doesNotContain(
      Map.entry("minValue", Set.of("minValue" + MinMaxValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  public void validate_positiveNumber() {
    var validationRequiredHints = List.of(new PositiveNumberHint());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, new MinMaxInput(String.valueOf(-2), String.valueOf(-1)), "My Property", List.of(), validationRequiredHints);
    assertThat(errorsMap).contains(
        Map.entry("minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode())),
        Map.entry("maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode()))
    );
  }

  @Test
  public void validate_integer() {
    var validationRequiredHints = List.of(new IntegerHint());
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, new MinMaxInput(String.valueOf(3.6), String.valueOf(5.6)), "My Property", List.of(), validationRequiredHints);
    assertThat(errorsMap).contains(
        Map.entry("minValue", Set.of("minValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode())),
        Map.entry("maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.NOT_INTEGER.getCode()))
    );
  }

  @Test
  public void validate_decimalPlaces_2dp() {
    var validationRequiredHints = List.of(new DecimalPlacesHint(2));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, new MinMaxInput(String.valueOf(3.444), String.valueOf(5.644)), "My Property", List.of(), validationRequiredHints);
    assertThat(errorsMap).contains(
        Map.entry("minValue", Set.of("minValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode())),
        Map.entry("maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode()))

    );
  }

  @Test
  public void validate_positiveAndDecimalPlaces_2dp() {
    var validationRequiredHints = List.of(new PositiveNumberHint(), new DecimalPlacesHint(2));
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator, new MinMaxInput(String.valueOf(-3.122), String.valueOf(5.644)), "My Property", List.of(), validationRequiredHints);
    assertThat(errorsMap).contains(
        Map.entry("maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode())),
        Map.entry("minValue", Set.of("minValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode(),
            "minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode()))
    );
  }

  @Test
  public void validate_validationTypeIsPartial_onlyValidateInputLength() {
    var validationRequiredHints = List.of(new DecimalPlacesHint(2), new PositiveNumberHint());
    var inputWithMoreDigitsThanAllowed = "9".repeat(MinMaxInputValidator.MAX_INPUT_LENGTH + 1);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(
      validator, new MinMaxInput(String.valueOf(-3.222), inputWithMoreDigitsThanAllowed), "My Property", List.of(), validationRequiredHints,
      ValidationType.PARTIAL);

    assertThat(errorsMap).contains(
      Map.entry("maxValue", Set.of("maxValue" + MinMaxValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    ).doesNotContain(
      Map.entry("minValue", Set.of("minValue" + MinMaxValidationErrorCodes.INVALID_DECIMAL_PLACE.getCode(),
        "minValue" + MinMaxValidationErrorCodes.NOT_POSITIVE.getCode()))
    );
  }

}