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
  private static final String OPTION_WORK_DESC_ATTR = "optionCompletedDescription";
  private static final String OTHER_WORK_DESC_ATTR = "otherWorkDescription";

  private static final String DESCRIPTION_OF_OPTION_MESSAGE_PREFIX = "Description of work";

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
        OPTION_WORK_DESC_ATTR,
        form::getOptionCompletedDescription,
        DESCRIPTION_OF_OPTION_MESSAGE_PREFIX
    );
  }

  private void validateOtherWorkDescriptionLength(Errors errors, ConfirmOptionForm form) {
    ValidatorUtils.validateDefaultStringLength(
        errors,
        OTHER_WORK_DESC_ATTR,
        form::getOtherWorkDescription,
        DESCRIPTION_OF_OPTION_MESSAGE_PREFIX
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
          OPTION_WORK_DESC_ATTR,
          REQUIRED.errorCode(OPTION_WORK_DESC_ATTR),
          "Describe the completed option and work done"
      );

      validateOptionDescriptionLength(errors, form);
    }

    if (ConfirmedOptionType.WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION.equals(form.getConfirmedOptionType())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(
          errors,
          OTHER_WORK_DESC_ATTR,
          REQUIRED.errorCode(OTHER_WORK_DESC_ATTR),
          "Describe the work done"
      );

      validateOtherWorkDescriptionLength(errors, form);
    }


  }

  private void validatePartial(Errors errors, ConfirmOptionForm form) {
    if (ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS.equals(form.getConfirmedOptionType())) {
      validateOptionDescriptionLength(errors, form);
    }

    if (ConfirmedOptionType.WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION.equals(form.getConfirmedOptionType())) {
      validateOtherWorkDescriptionLength(errors, form);
    }
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ConfirmOptionForm.class.equals(clazz);
  }
}
