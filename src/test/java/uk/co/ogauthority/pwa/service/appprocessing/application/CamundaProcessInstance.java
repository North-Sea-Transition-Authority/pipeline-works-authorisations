package uk.co.ogauthority.pwa.service.appprocessing.application;

import org.camunda.bpm.engine.runtime.ProcessInstance;

public class CamundaProcessInstance implements ProcessInstance {
  @Override
  public String getProcessDefinitionId() {
    return null;
  }

  @Override
  public String getBusinessKey() {
    return null;
  }

  @Override
  public String getRootProcessInstanceId() {
    return null;
  }

  @Override
  public String getCaseInstanceId() {
    return null;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public boolean isSuspended() {
    return false;
  }

  @Override
  public boolean isEnded() {
    return false;
  }

  @Override
  public String getProcessInstanceId() {
    return null;
  }

  @Override
  public String getTenantId() {
    return null;
  }
}
