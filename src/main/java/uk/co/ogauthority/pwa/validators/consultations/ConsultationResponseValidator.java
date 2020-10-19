package uk.co.ogauthority.pwa.validators.consultations;


import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ConsultationResponseValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ConsultationResponseForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }


  @Override
  public void validate(Object target, Errors errors) {
    var form = (ConsultationResponseForm) target;

    ValidationUtils.rejectIfEmpty(errors,"consultationResponseOption",
        FieldValidationErrorCodes.REQUIRED.errorCode("consultationResponseOption"),
        "You must confirm or reject this application");

    if (form.getConsultationResponseOption() == ConsultationResponseOption.REJECTED)  {
      ValidationUtils.rejectIfEmpty(errors,"rejectedDescription", FieldValidationErrorCodes.REQUIRED.errorCode("rejectedDescription"),
          "You must provide a reason for rejecting this application");
    }

    ValidatorUtils.validateDefaultStringLength(
        errors,
        "confirmedDescription",
        form::getConfirmedDescription,
        "Consent conditions");

    ValidatorUtils.validateDefaultStringLength(
        errors,
        "rejectedDescription",
        form::getRejectedDescription,
        "Reason for rejecting the application");

  }



}
