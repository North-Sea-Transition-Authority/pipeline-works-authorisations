package uk.co.ogauthority.pwa.validators.consultations;


import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
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

    var form = (ConsultationResponseForm) target;
    var requestDto = (ConsultationRequestDto) validationHints[0];

    // todo pwa-1359 expand for EMT changes
    requestDto.getConsultationResponseOptionGroups().forEach(consultationResponseOptionGroup -> {

      ValidationUtils.rejectIfEmpty(errors,"consultationResponseOption",
          FieldValidationErrorCodes.REQUIRED.errorCode("consultationResponseOption"),
          "Select a response decision");

      if (form.getConsultationResponseOption() == ConsultationResponseOption.REJECTED)  {
        ValidationUtils.rejectIfEmpty(errors,"option2Description", FieldValidationErrorCodes.REQUIRED.errorCode("option2Description"),
            "Enter a reason for rejecting this application");
      }

      if (form.getConsultationResponseOption() == ConsultationResponseOption.PROVIDE_ADVICE) {
        ValidationUtils.rejectIfEmpty(errors, "option1Description", FieldValidationErrorCodes.REQUIRED.errorCode("option1Description"),
            "Enter some advice text");
      }

      ValidatorUtils.validateDefaultStringLength(
          errors,
          "option1Description",
          form::getOption1Description,
          consultationResponseOptionGroup.getResponseOptionNumber(1).getTextAreaLengthValidationMessagePrefix());

      ValidatorUtils.validateDefaultStringLength(
          errors,
          "option2Description",
          form::getOption2Description,
          consultationResponseOptionGroup.getResponseOptionNumber(2).getTextAreaLengthValidationMessagePrefix());

    });

  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Use method with hints");
  }

}
