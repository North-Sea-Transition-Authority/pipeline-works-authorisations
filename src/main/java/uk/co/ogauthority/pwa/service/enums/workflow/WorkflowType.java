package uk.co.ogauthority.pwa.service.enums.workflow;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.WorkflowException;

/**
 * Enumeration of Camunda workflows.
 * processDefinitionKey = name attributed to process in .bpmn document
 * workflowTaskClass = class that stores enumeration of tasks for the workflow
 */
public enum WorkflowType {

  PWA_APPLICATION("pwaApplication", PwaApplicationWorkflowTask.class),
  PWA_APPLICATION_CONSULTATION("pwaApplicationConsultation", PwaApplicationConsultationWorkflowTask.class),
  PWA_APPLICATION_PUBLIC_NOTICE("pwaApplicationPublicNotice", PwaApplicationPublicNoticeWorkflowTask.class);

  private final String processDefinitionKey;
  private final Class<? extends UserWorkflowTask> workflowTaskClass;

  WorkflowType(String processDefinitionKey, Class<? extends UserWorkflowTask> workflowTaskClass) {
    this.processDefinitionKey = processDefinitionKey;
    this.workflowTaskClass = workflowTaskClass;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public Class<? extends UserWorkflowTask> getWorkflowTaskClass() {
    return workflowTaskClass;
  }

  public static WorkflowType resolveFromProcessDefinitionKey(String processDefinitionKey) {
    return Stream.of(WorkflowType.values())
        .filter(workflowType -> workflowType.getProcessDefinitionKey().equals(processDefinitionKey))
        .findFirst()
        .orElseThrow(() -> new WorkflowException(String.format("Couldn't resolve procDef key [%s] to a WorkflowType value",
            processDefinitionKey)));
  }

  public static WorkflowType resolveFromTaskWorkflowClass(Class<? extends UserWorkflowTask> workflowTaskClass) {
    return Stream.of(WorkflowType.values())
        .filter(workflowType -> workflowType.getWorkflowTaskClass().equals(workflowTaskClass))
        .findFirst()
        .orElseThrow(() -> new WorkflowException(String.format("Couldn't resolve workflowTaskClass [%s] to a WorkflowType value",
            workflowTaskClass.getName())));
  }

}
