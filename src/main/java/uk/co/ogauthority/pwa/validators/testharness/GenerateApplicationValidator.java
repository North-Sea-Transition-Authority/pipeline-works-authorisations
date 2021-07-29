package uk.co.ogauthority.pwa.validators.testharness;

import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.testharness.GenerateApplicationForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class GenerateApplicationValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return GenerateApplicationForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (GenerateApplicationForm) target;
    var appTypesForPipelines = (Set<PwaApplicationType>) validationHints[0];

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "applicationType",
        "applicationType" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select an application type");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "applicationStatus",
        "applicationStatus" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select an application status");

    if (appTypesForPipelines.contains(form.getApplicationType())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pipelineQuantity",
          "pipelineQuantity" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter a number of pipelines to generate");
    }

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "assignedCaseOfficerId",
        "assignedCaseOfficerId" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select a case officer");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "applicantPersonId",
        "applicantPersonId" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select an applicant");
  }



  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Validation method with hints must be used");
  }


}
