package uk.co.ogauthority.pwa.validators;

import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
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
        "You must enter a deposit reference");

    if (StringUtils.isNotBlank(form.getReference()) && validationHints[0] instanceof DepositDrawingsService) {
      var depositDrawingsService = (DepositDrawingsService) validationHints[0];
      var pwaApplicationDetail = (PwaApplicationDetail) validationHints[1];
      var padDepositDrawingId = validationHints.length >= 3 && validationHints[2] instanceof Integer ? (Integer) validationHints[2] : null;
      ValidatorUtils.validateBooleanTrue(errors, depositDrawingsService.isDrawingReferenceUnique(
          form.getReference(), padDepositDrawingId, pwaApplicationDetail),
          "reference", "Drawing reference must be unique, enter a different reference");
    }

    if (ListUtils.emptyIfNull(form.getUploadedFileWithDescriptionForms()).size() != 1) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode(),
          "You must upload one drawing");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "selectedDeposits", "selectedDeposits.required",
        "You must select at least one deposit");

  }





}
