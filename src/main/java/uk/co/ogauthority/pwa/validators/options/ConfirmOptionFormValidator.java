package uk.co.ogauthority.pwa.validators.options;

import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.ConfirmOptionForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ConfirmOptionFormValidator implements SmartValidator {

  private static final String OPTION_ATTR = "confirmedOptionType";
  private static final String OPTION_DESC_ATTR = "optionCompletedDescription";

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (ConfirmOptionForm) target;
    var validationType = (ValidationType) validationHints[0];

    if (validationType.equals(ValidationType.FULL)) {
      validateFull(errors, form);
    } else {
      validatePartial(errors, form);
    }

  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new ActionNotAllowedException("Required to use Smart Validator interface only as hints necessary");
  }

  private void validateOptionDescriptionLength(Errors errors, ConfirmOptionForm form) {
    ValidatorUtils.validateDefaultStringLength(
        errors,
        OPTION_DESC_ATTR,
        form::getOptionCompletedDescription,
        "description of option"
    );
  }

  private void validateFull(Errors errors, ConfirmOptionForm form) {
    if (form.getConfirmedOptionType() == null) {
      errors.rejectValue(
          OPTION_ATTR,
          REQUIRED.errorCode(OPTION_ATTR),
          "Select the option describing what has been done"
      );
    }

    if (ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS.equals(form.getConfirmedOptionType())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(
          errors,
          OPTION_DESC_ATTR,
          REQUIRED.errorCode(OPTION_DESC_ATTR),
          "Describe the completed option and work done"
      );

      validateOptionDescriptionLength(errors, form);
    }


  }

  private void validatePartial(Errors errors, ConfirmOptionForm form) {
    if (ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS.equals(form.getConfirmedOptionType())) {
      validateOptionDescriptionLength(errors, form);
    }
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ConfirmOptionForm.class.equals(clazz);
  }
}
