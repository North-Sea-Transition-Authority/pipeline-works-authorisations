package uk.co.ogauthority.pwa.service.appprocessing.consentreview;

import java.util.Objects;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent.ConsentReviewReturnForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ConsentReviewReturnFormValidator implements SmartValidator {

  private final WorkflowAssignmentService workflowAssignmentService;

  @Autowired
  public ConsentReviewReturnFormValidator(WorkflowAssignmentService workflowAssignmentService) {
    this.workflowAssignmentService = workflowAssignmentService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ConsentReviewReturnForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (ConsentReviewReturnForm) target;
    var application = (PwaApplication) validationHints[0];

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "caseOfficerPersonId",
        FieldValidationErrorCodes.REQUIRED.errorCode("caseOfficerPersonId"),
        "Select a case officer"
    );

    if (form.getCaseOfficerPersonId() != null) {

      boolean selectedUserCanBeAssigned = workflowAssignmentService
          .getAssignmentCandidates(application, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW).stream()
          .anyMatch(person -> Objects.equals(person.getId().asInt(), form.getCaseOfficerPersonId()));

      if (!selectedUserCanBeAssigned) {
        errors.rejectValue(
            "caseOfficerPersonId",
            FieldValidationErrorCodes.INVALID.errorCode("caseOfficerPersonId"),
            "This user is no longer a case officer. Select another case officer."
        );
      }

    }

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "returnReason",
        FieldValidationErrorCodes.REQUIRED.errorCode("returnReason"),
        "Enter a reason for sending back to the case officer"
    );

    ValidatorUtils.validateDefaultStringLength(
        errors, "returnReason", form::getReturnReason, "Reason for sending back to the case officer");

  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Use the other validate method, hints are required.");
  }

}
