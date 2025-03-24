package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;


@Service
public class PermanentDepositsDrawingValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PermanentDepositDrawingForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
  }

  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    var form = (PermanentDepositDrawingForm) o;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "reference", "reference.required",
        "Enter a deposit reference");

    ValidatorUtils.validateMaxStringLength(errors, "reference", form::getReference, "Drawing reference", 150);

    if (StringUtils.isNotBlank(form.getReference()) && validationHints[0] instanceof DepositDrawingsService) {
      var depositDrawingsService = (DepositDrawingsService) validationHints[0];
      var pwaApplicationDetail = (PwaApplicationDetail) validationHints[1];
      var padDepositDrawingId = validationHints.length >= 3 && validationHints[2] instanceof Integer ? (Integer) validationHints[2] : null;
      ValidatorUtils.validateBooleanTrue(errors, depositDrawingsService.isDrawingReferenceUnique(
          form.getReference(), padDepositDrawingId, pwaApplicationDetail),
          "reference", "Drawing reference must be unique, enter a different reference");
    }

    FileValidationUtils.validator()
        .withMinimumNumberOfFiles(1, "Upload a deposit drawing")
        .withMaximumNumberOfFiles(1, "Upload a maximum of one file")
        .validate(errors, form.getUploadedFiles());

    if (form.getSelectedDeposits() == null || form.getSelectedDeposits().isEmpty()) {
      errors.rejectValue("selectedDeposits",
          FieldValidationErrorCodes.REQUIRED.errorCode("selectedDeposits"),
          "Select at least one deposit");
    }

  }





}
