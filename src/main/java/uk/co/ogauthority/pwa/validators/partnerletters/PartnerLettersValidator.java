package uk.co.ogauthority.pwa.validators.partnerletters;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class PartnerLettersValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return PartnerLettersForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }


  @Override
  public void validate(Object target, Errors errors) {
    var form = (PartnerLettersForm) target;

    if (form.getPartnerLettersRequired() == null) {
      errors.rejectValue("partnerLettersRequired", "partnerLettersRequired" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select yes if you need to provide partner approval letters");
    }

    if (BooleanUtils.isTrue(form.getPartnerLettersRequired())) {
      if (ListUtils.emptyIfNull(form.getUploadedFileWithDescriptionForms()).isEmpty()) {
        errors.rejectValue("uploadedFileWithDescriptionForms",
            "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "You must upload at least one letter");

      } else {
        for (int x = 0; x < form.getUploadedFileWithDescriptionForms().size(); x++) {
          ValidationUtils.rejectIfEmptyOrWhitespace(errors, "uploadedFileWithDescriptionForms[" + x + "].uploadedFileDescription",
              "uploadedFileWithDescriptionForms[" + x + "].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode(),
              "You must enter a description for the uploaded letter");
        }
      }

      if (!BooleanUtils.isTrue(form.getPartnerLettersConfirmed())) {
        errors.rejectValue("partnerLettersConfirmed", "partnerLettersConfirmed" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "You must confirm that you have provided all required partner approval letters");
      }
    }
  }

}
