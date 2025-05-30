package uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.integrations.camunda.external.UserWorkflowTask;

public enum PwaApplicationWorkflowTask implements UserWorkflowTask {

  PREPARE_APPLICATION("prepareApplication", null),
  APPLICATION_REVIEW("applicationReview", null),
  AWAIT_APPLICATION_PAYMENT("awaitApplicationPayment", null),
  CASE_OFFICER_REVIEW("caseOfficerReview", WorkflowAssignment.CASE_OFFICER),
  CONSENT_REVIEW("consentReview", null),
  ISSUING_CONSENT("issuingConsent", null),
  AWAIT_FEEDBACK("awaitFeedback", null),
  UPDATE_APPLICATION("updateApplication", null);

  private final String taskKey;
  private final WorkflowAssignment workflowAssignment;

  PwaApplicationWorkflowTask(String taskKey,
                             WorkflowAssignment workflowAssignment) {
    this.taskKey = taskKey;
    this.workflowAssignment = workflowAssignment;
  }

  @Override
  public WorkflowAssignment getAssignment() {
    return workflowAssignment;
  }

  @Override
  public String getTaskName() {
    return name();
  }

  @Override
  public String getTaskKey() {
    return taskKey;
  }

  public static PwaApplicationWorkflowTask getByTaskKey(String taskKey) {
    return Stream.of(PwaApplicationWorkflowTask.values())
        .filter(val -> val.getTaskKey().equals(taskKey))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(String.format("Couldn't get PwaApplicationWorkflowTask from taskKey: %s", taskKey)));
  }

}
