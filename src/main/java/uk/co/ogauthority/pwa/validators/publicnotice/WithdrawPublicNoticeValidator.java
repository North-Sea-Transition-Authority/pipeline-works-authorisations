package uk.co.ogauthority.pwa.validators.publicnotice;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.publicnotice.WithdrawPublicNoticeForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class WithdrawPublicNoticeValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return WithdrawPublicNoticeForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }



  @Override
  public void validate(Object target, Errors errors) {
    var form = (WithdrawPublicNoticeForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "withdrawalReason", "withdrawalReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Enter a reason for withdrawing this public notice");

    ValidatorUtils.validateDefaultStringLength(
        errors, "withdrawalReason", form::getWithdrawalReason, "Withdrawal reason");


  }


}
