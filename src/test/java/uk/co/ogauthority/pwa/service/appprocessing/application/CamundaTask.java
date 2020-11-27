package uk.co.ogauthority.pwa.service.appprocessing.application;

import java.util.Date;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;



public class CamundaTask implements Task {

  @Override
  public String getId() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void setName(String name) {

  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void setDescription(String description) {

  }

  @Override
  public int getPriority() {
    return 0;
  }

  @Override
  public void setPriority(int priority) {

  }

  @Override
  public String getOwner() {
    return null;
  }

  @Override
  public void setOwner(String owner) {

  }

  @Override
  public String getAssignee() {
    return null;
  }

  @Override
  public void setAssignee(String assignee) {

  }

  @Override
  public DelegationState getDelegationState() {
    return null;
  }

  @Override
  public void setDelegationState(DelegationState delegationState) {

  }

  @Override
  public String getProcessInstanceId() {
    return null;
  }

  @Override
  public String getExecutionId() {
    return null;
  }

  @Override
  public String getProcessDefinitionId() {
    return null;
  }

  @Override
  public String getCaseInstanceId() {
    return null;
  }

  @Override
  public void setCaseInstanceId(String caseInstanceId) {

  }

  @Override
  public String getCaseExecutionId() {
    return null;
  }

  @Override
  public String getCaseDefinitionId() {
    return null;
  }

  @Override
  public Date getCreateTime() {
    return null;
  }

  @Override
  public String getTaskDefinitionKey() {
    return null;
  }

  @Override
  public Date getDueDate() {
    return null;
  }

  @Override
  public void setDueDate(Date dueDate) {

  }

  @Override
  public Date getFollowUpDate() {
    return null;
  }

  @Override
  public void setFollowUpDate(Date followUpDate) {

  }

  @Override
  public void delegate(String userId) {

  }

  @Override
  public void setParentTaskId(String parentTaskId) {

  }

  @Override
  public String getParentTaskId() {
    return null;
  }

  @Override
  public boolean isSuspended() {
    return false;
  }

  @Override
  public String getFormKey() {
    return null;
  }

  @Override
  public String getTenantId() {
    return null;
  }

  @Override
  public void setTenantId(String tenantId) {

  }
}
