package uk.co.ogauthority.pwa.validators.appprocessing.initialreview;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.appprocessing.initialreview.InitialReviewForm;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;

@Service
public class InitialReviewFormValidator implements Validator {

  private final WorkflowAssignmentService assignmentService;

  @Autowired
  public InitialReviewFormValidator(WorkflowAssignmentService assignmentService) {
    this.assignmentService = assignmentService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(InitialReviewForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {

    var form = (InitialReviewForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "caseOfficerPersonId", "caseOfficerPersonId.required",
        "Select a case officer");

    if (form.getCaseOfficerPersonId() != null) {

      boolean selectedUserCanBeAssigned = assignmentService.getAssignmentCandidates(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW).stream()
          .anyMatch(person -> Objects.equals(person.getId().asInt(), form.getCaseOfficerPersonId()));

      if (!selectedUserCanBeAssigned) {
        errors.rejectValue("caseOfficerPersonId", "caseOfficerPersonId.invalid",
            "This user is no longer a case officer. Select another case officer.");
      }

    }

  }

}
