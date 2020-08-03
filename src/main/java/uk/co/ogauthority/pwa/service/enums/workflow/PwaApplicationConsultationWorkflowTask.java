package uk.co.ogauthority.pwa.service.enums.workflow;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;

public enum PwaApplicationConsultationWorkflowTask implements UserWorkflowTask {

  ALLOCATION("allocation", null),
  RESPONSE("response", WorkflowAssignment.CONSULTATION_RESPONDER);

  private final String taskKey;
  private final WorkflowAssignment workflowAssignment;

  PwaApplicationConsultationWorkflowTask(String taskKey,
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

  public static PwaApplicationConsultationWorkflowTask getByTaskKey(String taskKey) {
    return Stream.of(PwaApplicationConsultationWorkflowTask.values())
        .filter(val -> val.getTaskKey().equals(taskKey))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(
            String.format("Couldn't get PwaApplicationConsultationWorkflowTask from taskKey: %s", taskKey)));
  }

}
