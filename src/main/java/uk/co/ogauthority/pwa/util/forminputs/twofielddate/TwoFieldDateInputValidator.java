package uk.co.ogauthority.pwa.util.forminputs.twofielddate;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;

@Component
public class TwoFieldDateInputValidator implements SmartValidator {

  private static final String MONTH = "month";
  private static final String YEAR = "year";

  private static final String MONTH_REQUIRED_CODE = MONTH + FieldValidationErrorCodes.REQUIRED.getCode();
  private static final String YEAR_REQUIRED_CODE = YEAR + FieldValidationErrorCodes.REQUIRED.getCode();

  private static final String MONTH_INVALID_CODE = MONTH + FieldValidationErrorCodes.INVALID.getCode();
  private static final String YEAR_INVALID_CODE = YEAR + FieldValidationErrorCodes.INVALID.getCode();

  private static final String MONTH_AFTER_DATE_CODE = MONTH + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode();
  private static final String YEAR_AFTER_DATE_CODE = YEAR + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode();

  private static final String MONTH_BEFORE_DATE_CODE = MONTH + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode();
  private static final String YEAR_BEFORE_DATE_CODE = YEAR + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode();

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(TwoFieldDateInput.class);
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
        .orElse(new FormInputLabel("Date"));

    var twoFieldDateInput = (TwoFieldDateInput) o;
    var dateOptional = twoFieldDateInput.createDate();

    Optional<OnOrBeforeDateHint> onOrBeforeDateHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(OnOrBeforeDateHint.class))
        .map(hint -> ((OnOrBeforeDateHint) hint))
        .findFirst();

    Optional<BeforeDateHint> beforeDateHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(BeforeDateHint.class))
        .map(hint -> ((BeforeDateHint) hint))
        .findFirst();

    Optional<OnOrAfterDateHint> onOrAfterDateHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(OnOrAfterDateHint.class))
        .map(hint -> ((OnOrAfterDateHint) hint))
        .findFirst();

    Optional<AfterDateHint> afterDateHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(AfterDateHint.class))
        .map(hint -> ((AfterDateHint) hint))
        .findFirst();

    Optional<DateWithinRangeHint> dateWithinRangeHint = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(DateWithinRangeHint.class))
        .map(hint -> ((DateWithinRangeHint) hint))
        .findFirst();


    if (dateOptional.isEmpty() && twoFieldDateInput.getMonth() == null && twoFieldDateInput.getYear() == null) {
      errors.rejectValue(
          MONTH,
          MONTH_REQUIRED_CODE,
          ""
      );
      errors.rejectValue(
          YEAR,
          YEAR_REQUIRED_CODE,
          formatValidationMessage(String.format(ValidatorUtils.DATE_REQUIRED_ERROR_FORMAT, inputLabel.getLabel())));

    } else if (dateOptional.isEmpty() || dateOptional.filter(date -> ValidatorUtils.isYearValid(date.getYear())).isEmpty()) {
      errors.rejectValue(
          MONTH,
          MONTH_INVALID_CODE,
          ""
      );
      errors.rejectValue(
          YEAR,
          YEAR_INVALID_CODE,
          String.format(ValidatorUtils.MONTH_YEAR_INVALID_ERROR_FORMAT, inputLabel.getLabel()));

    } else {
      // only do additional validation when the date is valid
      afterDateHint.ifPresent(hint -> validateAfterDate(errors, twoFieldDateInput, inputLabel, hint));
      beforeDateHint.ifPresent(hint -> validateBeforeDate(errors, twoFieldDateInput, inputLabel, hint));
      onOrAfterDateHint.ifPresent(hint -> validateOnOrAfterDate(errors, twoFieldDateInput, inputLabel, hint));
      onOrBeforeDateHint.ifPresent(hint -> validateOnOrBeforeDate(errors, twoFieldDateInput, inputLabel, hint));
      dateWithinRangeHint.ifPresent(hint -> validateDateWithinRange(errors, twoFieldDateInput, inputLabel, hint));
    }

  }

  /* this validation message formatting covers an edge case where we have the word 'date' duplicated with a space in between
   depending on the input label provided. This method removes the duplicate if exists
   */
  private String formatValidationMessage(String message) {

    var searchWord = "date";
    var fromIndex = 0;
    var searchWordFrequency = 0;

    while ((fromIndex = message.toLowerCase().indexOf(searchWord, fromIndex)) != -1) {
      searchWordFrequency++;

      //we have found the duplicate word adjacent(inc. space) to the first occurrence, remove second occurrence
      if (searchWordFrequency == 2) {
        StringBuilder stringBuilder = new StringBuilder(message);
        stringBuilder.replace(fromIndex, fromIndex + searchWord.length(), "");
        return stringBuilder.toString().trim();
      }
      fromIndex = fromIndex + searchWord.length() + 1;
    }

    return message;
  }


  // There must be a cleaner way than this to avoid adding new methods per hint type.
  // Maybe put the check on the date hints them selves and loop over any that exist?
  // Revisit if any more get added
  // Things to look at, moving error code onto hint, moving message format string to hint,
  // can we move check itself to hint without having to add explicit input object classes to the generic date hints?
  // ^would that be better or worse than what we have? ie treating hints as validation strategies
  private void validateOnOrAfterDate(Errors errors,
                                     TwoFieldDateInput twoFieldDateInput,
                                     FormInputLabel inputLabel,
                                     OnOrAfterDateHint testOnOrAfterDate) {
    if (!(
        twoFieldDateInput.isAfter(testOnOrAfterDate.getDate())
            || twoFieldDateInput.isInSameMonth(testOnOrAfterDate.getDate())
      )) {
      var afterDateLabel = testOnOrAfterDate.getDateLabel();

      errors.rejectValue(MONTH, MONTH_AFTER_DATE_CODE, "");
      errors.rejectValue(YEAR, YEAR_AFTER_DATE_CODE,
          StringUtils.capitalize(inputLabel.getLabel()) + " must be the same as or after " + afterDateLabel);
    }
  }

  private void validateOnOrBeforeDate(Errors errors,
                                      TwoFieldDateInput twoFieldDateInput,
                                      FormInputLabel inputLabel,
                                      OnOrBeforeDateHint testOnOrBeforeDate) {
    if (!(
        twoFieldDateInput.isBefore(testOnOrBeforeDate.getDate())
            || twoFieldDateInput.isInSameMonth(testOnOrBeforeDate.getDate())
      )) {
      var beforeDateLabel = testOnOrBeforeDate.getDateLabel();

      errors.rejectValue(MONTH, MONTH_BEFORE_DATE_CODE, "");
      errors.rejectValue(YEAR, YEAR_BEFORE_DATE_CODE,
          StringUtils.capitalize(inputLabel.getLabel()) + " must be the same as or before " + beforeDateLabel);
    }
  }

  private void validateAfterDate(Errors errors,
                                 TwoFieldDateInput twoFieldDateInput,
                                 FormInputLabel inputLabel,
                                 AfterDateHint testAfterDate) {
    if (!twoFieldDateInput.isAfter(testAfterDate.getDate())) {
      var afterDateLabel = testAfterDate.getDateLabel();

      errors.rejectValue(MONTH, MONTH_AFTER_DATE_CODE, "");
      errors.rejectValue(YEAR, YEAR_AFTER_DATE_CODE, StringUtils.capitalize(inputLabel.getLabel()) + " must be after " + afterDateLabel);
    }
  }

  private void validateBeforeDate(Errors errors,
                                  TwoFieldDateInput twoFieldDateInput,
                                  FormInputLabel inputLabel,
                                  BeforeDateHint beforeDateHint) {
    if (!twoFieldDateInput.isBefore(beforeDateHint.getDate())) {
      var beforeDateLabel = beforeDateHint.getDateLabel();

      errors.rejectValue(MONTH, MONTH_BEFORE_DATE_CODE, "");
      errors.rejectValue(YEAR, YEAR_BEFORE_DATE_CODE, StringUtils.capitalize(inputLabel.getLabel()) + " must be before " + beforeDateLabel);
    }
  }


  private boolean validateDateWithinRange(Errors errors,
                                           TwoFieldDateInput twoFieldDateInput,
                                           FormInputLabel inputLabel,
                                           DateWithinRangeHint withinRangeHint) {
    if (twoFieldDateInput.isBefore(withinRangeHint.getFromDate()) || twoFieldDateInput.isAfter(withinRangeHint.getToDate())) {
      errors.rejectValue(MONTH, MONTH + FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.getCode(), "");
      errors.rejectValue(YEAR, YEAR + FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.getCode(),
          inputLabel.getLabel() + " must be within the range of " + withinRangeHint.getRangeDescription());
      return false;
    }
    return true;

  }


}
