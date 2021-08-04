package uk.co.ogauthority.pwa.validators.testharness;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.testharness.GenerateApplicationForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessService;

@Service
public class GenerateApplicationValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return GenerateApplicationForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }



  @Override
  public void validate(Object target, Errors errors) {
    var form = (GenerateApplicationForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "applicationType",
        "applicationType" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select an application type");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "applicationStatus",
        "applicationStatus" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select an application status");

    if (TestHarnessService.getAppTypesForPipelines().contains(form.getApplicationType())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pipelineQuantity",
          "pipelineQuantity" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter a number of pipelines to generate");
    }

    if (TestHarnessService.getAppStatusesForCaseOfficer().contains(form.getApplicationStatus())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "assignedCaseOfficerId",
          "assignedCaseOfficerId" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select a case officer");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "applicantPersonId",
        "applicantPersonId" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select an applicant");
  }


}
