package uk.co.ogauthority.pwa.integrations.camunda.external;

import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

/**
 * Data class to store an instance of a workflow task and the user assigned to it.
 */
public class AssignedTaskInstance {

  private final WorkflowTaskInstance workflowTaskInstance;
  private final Person assignee;

  public AssignedTaskInstance(WorkflowTaskInstance workflowTaskInstance, Person assignee) {
    this.workflowTaskInstance = workflowTaskInstance;
    this.assignee = assignee;
  }

  public Integer getBusinessKey() {
    return workflowTaskInstance.getBusinessKey();
  }

  public String getTaskDefinitionKey() {
    return workflowTaskInstance.getTaskKey();
  }

  public WorkflowType getWorkflowType() {
    return workflowTaskInstance.getWorkflowType();
  }

  public Person getAssignee() {
    return assignee;
  }

}
