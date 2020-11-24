package uk.co.ogauthority.pwa.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class WithdrawApplicationValidator implements SmartValidator {


  @Autowired
  public WithdrawApplicationValidator() {}

  @Override
  public boolean supports(Class<?> clazz) {
    return WithdrawApplicationForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (WithdrawApplicationForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "withdrawalReason", "withdrawalReason.required",
        "Enter a reason for why you are withdrawing this application");

    ValidatorUtils.validateDefaultStringLength(
        errors, "withdrawalReason", form::getWithdrawalReason, "Reason");

  }


  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }
}
