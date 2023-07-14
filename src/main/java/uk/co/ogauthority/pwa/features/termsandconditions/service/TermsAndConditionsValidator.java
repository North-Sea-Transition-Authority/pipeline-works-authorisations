package uk.co.ogauthority.pwa.features.termsandconditions.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class TermsAndConditionsValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return TermsAndConditionsForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (TermsAndConditionsForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "pwaId",
        FieldValidationErrorCodes.REQUIRED.errorCode("pwaId"),
        "Enter the PWA reference");

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "variationTerm",
        FieldValidationErrorCodes.REQUIRED.errorCode("variationTerm"),
        "Enter the variation term");

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "huooTermOne",
        FieldValidationErrorCodes.REQUIRED.errorCode("huooTermOne"),
        "Enter HUOO term one");

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "huooTermTwo",
        FieldValidationErrorCodes.REQUIRED.errorCode("huooTermTwo"),
        "Enter HUOO term two");

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "huooTermThree",
        FieldValidationErrorCodes.REQUIRED.errorCode("huooTermThree"),
        "Enter HUOO term three");

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "depconParagraph",
        FieldValidationErrorCodes.REQUIRED.errorCode("depconParagraph"),
        "Enter the depcon paragraph");

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "depconSchedule",
        FieldValidationErrorCodes.REQUIRED.errorCode("depconSchedule"),
        "Enter the depcon schedule");
  }
}