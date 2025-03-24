package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class PartnerLettersValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return PartnerLettersForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var validationType = (ValidationType) validationHints[0];
    var form = (PartnerLettersForm) target;

    if (validationType.equals(ValidationType.FULL)) {

      if (form.getPartnerLettersRequired() == null) {
        errors.rejectValue("partnerLettersRequired",
            "partnerLettersRequired" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Select yes if you need to provide partner approval letters");
      }

      if (BooleanUtils.isTrue(form.getPartnerLettersRequired())) {

        FileValidationUtils.validator()
            .withMinimumNumberOfFiles(1, "Upload at least one letter")
            .validate(errors, form.getUploadedFiles());

        if (!BooleanUtils.isTrue(form.getPartnerLettersConfirmed())) {
          errors.rejectValue("partnerLettersConfirmed",
              "partnerLettersConfirmed" + FieldValidationErrorCodes.REQUIRED.getCode(),
              "Confirm that you have provided all required partner approval letters");
        }
      }

    } else {
      FileValidationUtils.validator()
          .validate(errors, form.getUploadedFiles());
    }
  }


  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Use the validate method with validation hints");
  }

}
