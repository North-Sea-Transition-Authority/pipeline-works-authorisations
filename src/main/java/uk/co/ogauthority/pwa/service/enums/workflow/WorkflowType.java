package uk.co.ogauthority.pwa.service.enums.workflow;

/**
 * Enumeration of Camunda workflows.
 * processDefinitionKey = name attributed to process in .bpmn document
 */
public enum WorkflowType {

  PWA_APPLICATION("pwaApplication");

  private String processDefinitionKey;

  WorkflowType(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

}
