package uk.co.ogauthority.pwa.validators.publicnotice;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

@Service
public class PublicNoticeDraftValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return PublicNoticeDraftForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }



  @Override
  public void validate(Object target, Errors errors) {
    var form = (PublicNoticeDraftForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "coverLetterText", "coverLetterText" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Enter the cover letter text");

    ValidatorUtils.validateDefaultStringLength(
        errors, "coverLetterText", form::getCoverLetterText, "Cover letter");

    FileUploadUtils.validateFiles(form, errors, List.of(MandatoryUploadValidation.class), "Upload a public notice document");
    FileUploadUtils.validateMaxFileLimit(form, errors, 1, "Upload a maximum of one file");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "reason", "reason" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select a reason for why the public notice is being requested");

    if (PublicNoticeRequestReason.CONSULTEES_NOT_ALL_CONTENT.equals(form.getReason())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "reasonDescription",
          "reasonDescription" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a description for why the public notice needs to progress");

      ValidatorUtils.validateDefaultStringLength(
          errors, "reasonDescription", form::getReasonDescription, "Description for why the public notice needs to progress");
    }
  }
}
