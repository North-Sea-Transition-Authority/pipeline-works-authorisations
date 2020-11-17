package uk.co.ogauthority.pwa.validators.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.AssignCaseOfficerForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;

@Service
public class AssignCaseOfficerValidator implements SmartValidator {

  private final WorkflowAssignmentService workflowAssignmentService;

  @Autowired
  public AssignCaseOfficerValidator(
      WorkflowAssignmentService workflowAssignmentService) {
    this.workflowAssignmentService = workflowAssignmentService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return AssignCaseOfficerForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (AssignCaseOfficerForm) target;
    var pwaApplication = (PwaApplication) validationHints[0];

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "caseOfficerPersonId",
        "caseOfficerPersonId" + FieldValidationErrorCodes.REQUIRED.getCode(), "Select a case officer");

    if (form.getCaseOfficerPersonId() != null && !isPersonValidAssignmentCandidate(pwaApplication, form.getCaseOfficerPersonId())) {
      errors.rejectValue("caseOfficerPersonId", "caseOfficerPersonId" + FieldValidationErrorCodes.INVALID.getCode(),
          "The selected case officer is invalid");
    }



  }


  private boolean isPersonValidAssignmentCandidate(PwaApplication pwaApplication, Integer caseOfficerPersonId) {
    var validCaseOfficer = workflowAssignmentService
        .getAssignmentCandidates(pwaApplication, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW).stream()
        .filter(caseOfficerPerson -> caseOfficerPersonId != null && caseOfficerPerson.getId().asInt() == caseOfficerPersonId)
        .findAny();
    return validCaseOfficer.isPresent();
  }

}
