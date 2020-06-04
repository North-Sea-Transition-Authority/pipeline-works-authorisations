package uk.co.ogauthority.pwa.service.workflow;

import java.util.Optional;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 * A workflow-agnostic service designed to provide generic access to common Camunda features.
 */
@Service
public class CamundaWorkflowService {

  private final RuntimeService runtimeService;
  private final TaskService taskService;

  @Autowired
  public CamundaWorkflowService(RuntimeService runtimeService,
                                TaskService taskService) {
    this.taskService = taskService;
    this.runtimeService = runtimeService;
  }

  public void startWorkflow(WorkflowSubject workflowSubject) {
    runtimeService.startProcessInstanceByKey(
        workflowSubject.getWorkflowType().getProcessDefinitionKey(),
        workflowSubject.getBusinessKey().toString());
  }

  private Optional<Task> getWorkflowTask(WorkflowTaskInstance workflowTaskInstance) {

    return Optional.ofNullable(taskService.createTaskQuery()
        .processDefinitionKey(workflowTaskInstance.getWorkflowType().getProcessDefinitionKey())
        .processInstanceBusinessKey(workflowTaskInstance.getBusinessKey().toString())
        .active()
        .taskDefinitionKey(workflowTaskInstance.getTaskKey())
        .singleResult());

  }

  public void completeTask(WorkflowTaskInstance workflowTaskInstance) {

    getWorkflowTask(workflowTaskInstance).ifPresentOrElse(
        foundTask -> taskService.complete(foundTask.getId()),
        () -> throwTaskNotFoundException(workflowTaskInstance)
    );

  }

  public void assignTaskToUser(WorkflowTaskInstance workflowTaskInstance, Person person) {

    getWorkflowTask(workflowTaskInstance).ifPresentOrElse(
        task -> taskService.setAssignee(task.getId(), String.valueOf(person.getId().asInt())),
        () -> throwTaskNotFoundException(workflowTaskInstance)
    );

  }

  private void throwTaskNotFoundException(WorkflowTaskInstance workflowTaskInstance) {
    throw new WorkflowException(
        String.format("Active task: [%s] not found for workflow: [%s] and business key: [%s]",
            workflowTaskInstance.getTaskKey(),
            workflowTaskInstance.getWorkflowType().name(),
            workflowTaskInstance.getBusinessKey().toString()));
  }

}
