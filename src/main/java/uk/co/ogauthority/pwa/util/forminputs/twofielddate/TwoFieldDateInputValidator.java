package uk.co.ogauthority.pwa.util.forminputs.twofielddate;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;

public class TwoFieldDateInputValidator implements SmartValidator {

  private static final String MONTH = "month";
  private static final String YEAR = "year";

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

    var twoFieldDateInput = (TwoFieldDateInput) o;

    // should be be small list of hints so this repeated looping over whole list is probably harmless
    var inputLabel = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(FormInputLabel.class))
        .map(hint -> ((FormInputLabel) hint))
        .findFirst()
        .orElse(new FormInputLabel("Date"));

    Optional<BeforeDateHint> testBeforeDate = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(BeforeDateHint.class))
        .map(hint -> ((BeforeDateHint) hint))
        .findFirst();

    Optional<AfterDateHint> testAfterDate = Arrays.stream(objects)
        .filter(hint -> hint.getClass().equals(AfterDateHint.class))
        .map(hint -> ((AfterDateHint) hint))
        .findFirst();

    if (twoFieldDateInput.createDate().isEmpty()) {
      errors.rejectValue(MONTH, MONTH_INVALID_CODE, "");
      errors.rejectValue(
          YEAR,
          YEAR_INVALID_CODE,
          inputLabel.getLabel() + " must be a valid date");
    } else {
      // only do additional validation when the date is valid
      testAfterDate.ifPresent(afterDateHint -> validateAfterDate(errors, twoFieldDateInput, inputLabel, afterDateHint));

      testBeforeDate.ifPresent(beforeDateHint -> validateBeforeDate(errors, twoFieldDateInput, inputLabel, beforeDateHint));
    }


  }

  private void validateAfterDate(Errors errors,
                                 TwoFieldDateInput twoFieldDateInput,
                                 FormInputLabel inputLabel,
                                 AfterDateHint testAfterDate) {
    if (!twoFieldDateInput.isAfter(testAfterDate.getDate())) {
      var afterDateLabel = testAfterDate.getDateLabel();

      errors.rejectValue(MONTH, MONTH_AFTER_DATE_CODE, "");
      errors.rejectValue(YEAR, YEAR_AFTER_DATE_CODE, inputLabel.getLabel() + " must be after " + afterDateLabel);
    }
  }


  private void validateBeforeDate(Errors errors,
                                  TwoFieldDateInput twoFieldDateInput,
                                  FormInputLabel inputLabel,
                                  BeforeDateHint beforeDateHint) {
    if (!twoFieldDateInput.isBefore(beforeDateHint.getDate())) {
      var beforeDateLabel = beforeDateHint.getDateLabel();

      errors.rejectValue(MONTH, MONTH_BEFORE_DATE_CODE, "");
      errors.rejectValue(YEAR, YEAR_BEFORE_DATE_CODE, inputLabel.getLabel() + " must be before " + beforeDateLabel);
    }
  }

}
