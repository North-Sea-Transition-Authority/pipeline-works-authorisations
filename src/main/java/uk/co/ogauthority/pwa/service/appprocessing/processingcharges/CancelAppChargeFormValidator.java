package uk.co.ogauthority.pwa.service.appprocessing.processingcharges;


import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.CancelAppChargeForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class CancelAppChargeFormValidator implements Validator {
  private static final String CANCEL_REASON_ATTR = "cancellationReason";

  @Override
  public boolean supports(Class<?> clazz) {
    return CancelAppChargeForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    var form = (CancelAppChargeForm) target;
    ValidationUtils.rejectIfEmpty(
        errors,
        CANCEL_REASON_ATTR,
        FieldValidationErrorCodes.REQUIRED.errorCode(CANCEL_REASON_ATTR),
        "Enter a reason for cancelling the payment request");
    ValidatorUtils.validateDefaultStringLength(errors, CANCEL_REASON_ATTR, form::getCancellationReason, "Cancellation reason");
  }
}
