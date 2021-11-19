package uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings;

import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;

public enum PwaApplicationWorkflowMessageEvents {

  UPDATE_APPLICATION_REQUEST("updateApplicationRequest"),
  // value with same event name as duplicate workflow requirements.
  OPTIONS_APPROVED("updateApplicationRequest");

  private final String messageEventName;

  PwaApplicationWorkflowMessageEvents(String messageEventName) {
    this.messageEventName = messageEventName;
  }

  public String getMessageEventName() {
    return messageEventName;
  }

  public static WorkflowType getWorkFlowType() {
    return WorkflowType.PWA_APPLICATION;
  }

}