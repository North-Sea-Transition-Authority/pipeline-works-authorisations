package uk.co.ogauthority.pwa.validators.publicnotice;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationPublicNoticeApprovalResult;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class PublicNoticeApprovalValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return PublicNoticeApprovalForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }



  @Override
  public void validate(Object target, Errors errors) {
    var form = (PublicNoticeApprovalForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestApproved", "requestApproved" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select 'Approve' if the public notice request should be approved");

    if (PwaApplicationPublicNoticeApprovalResult.REQUEST_REJECTED.equals(form.getRequestApproved())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestRejectedReason",
          "requestRejectedReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter a reason for why the public notice request is being rejected");

      ValidatorUtils.validateDefaultStringLength(
          errors, "requestRejectedReason", form::getRequestRejectedReason, "Rejection reason");
    }


  }


}
