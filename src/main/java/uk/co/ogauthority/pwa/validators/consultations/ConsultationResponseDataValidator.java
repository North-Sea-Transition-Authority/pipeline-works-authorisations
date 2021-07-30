package uk.co.ogauthority.pwa.validators.consultations;


import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ConsultationResponseDataValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ConsultationResponseDataForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (ConsultationResponseDataForm) target;
    var responseOptionGroup = (ConsultationResponseOptionGroup) validationHints[0];

    if (responseOptionGroup == ConsultationResponseOptionGroup.EIA_REGS) {
      ValidationUtils.rejectIfEmpty(errors,"consultationResponseOption",
          FieldValidationErrorCodes.REQUIRED.errorCode("consultationResponseOption"),
          "Select a response decision under EIA regulations");
    } else if (responseOptionGroup == ConsultationResponseOptionGroup.HABITATS_REGS) {
      ValidationUtils.rejectIfEmpty(errors,"consultationResponseOption",
          FieldValidationErrorCodes.REQUIRED.errorCode("consultationResponseOption"),
          "Select a response decision under Habitats regulations");
    } else {
      ValidationUtils.rejectIfEmpty(errors,"consultationResponseOption",
          FieldValidationErrorCodes.REQUIRED.errorCode("consultationResponseOption"),
          "Select a response decision");
    }

    if (form.getConsultationResponseOption() == ConsultationResponseOption.REJECTED)  {
      ValidationUtils.rejectIfEmpty(errors,"option2Description", FieldValidationErrorCodes.REQUIRED.errorCode("option2Description"),
          "Enter a reason for rejecting this application");
    }

    if (form.getConsultationResponseOption() == ConsultationResponseOption.PROVIDE_ADVICE) {
      ValidationUtils.rejectIfEmpty(errors, "option1Description", FieldValidationErrorCodes.REQUIRED.errorCode("option1Description"),
          "Enter some advice text");
    }

    if (form.getConsultationResponseOption() == ConsultationResponseOption.EIA_DISAGREE) {
      ValidationUtils.rejectIfEmpty(errors,"option2Description", FieldValidationErrorCodes.REQUIRED.errorCode("option2Description"),
          "Enter a reason for not agreeing to this application under EIA regulations");
    }

    if (form.getConsultationResponseOption() == ConsultationResponseOption.HABITATS_DISAGREE) {
      ValidationUtils.rejectIfEmpty(errors,"option2Description", FieldValidationErrorCodes.REQUIRED.errorCode("option2Description"),
          "Enter a reason for not agreeing to this application under Habitats regulations");
    }

    if (form.getConsultationResponseOption() == ConsultationResponseOption.EIA_NOT_RELEVANT) {
      ValidationUtils.rejectIfEmpty(errors,"option3Description", FieldValidationErrorCodes.REQUIRED.errorCode("option3Description"),
          "Enter a reason why agreement is not required under EIA regulations");
    }

    if (form.getConsultationResponseOption() == ConsultationResponseOption.HABITATS_NOT_RELEVANT) {
      ValidationUtils.rejectIfEmpty(errors,"option3Description", FieldValidationErrorCodes.REQUIRED.errorCode("option3Description"),
          "Enter a reason why agreement is not required under Habitats regulations");
    }

    responseOptionGroup.getResponseOptionNumber(1).ifPresent(responseOption ->
        ValidatorUtils.validateDefaultStringLength(
            errors,
            "option1Description",
            form::getOption1Description,
            responseOption.getTextAreaLengthValidationMessagePrefix()));

    responseOptionGroup.getResponseOptionNumber(2).ifPresent(responseOption ->
        ValidatorUtils.validateDefaultStringLength(
            errors,
            "option2Description",
            form::getOption2Description,
            responseOption.getTextAreaLengthValidationMessagePrefix()));

    responseOptionGroup.getResponseOptionNumber(3).ifPresent(responseOption ->
        ValidatorUtils.validateDefaultStringLength(
            errors,
            "option3Description",
            form::getOption3Description,
            responseOption.getTextAreaLengthValidationMessagePrefix()));

  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Use method with hints");
  }

}
