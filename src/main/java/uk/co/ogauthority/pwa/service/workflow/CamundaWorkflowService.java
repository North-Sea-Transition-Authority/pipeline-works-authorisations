package uk.co.ogauthority.pwa.service.workflow;

import java.util.Optional;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;

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

  public void startWorkflow(WorkflowType workflowType, Integer businessKey) {
    runtimeService.startProcessInstanceByKey(workflowType.getProcessDefinitionKey(), businessKey.toString());
  }

  public void completeTask(Integer businessKey, UserWorkflowTask task) {

    Optional<Task> taskToComplete = Optional.ofNullable(taskService
        .createTaskQuery()
        .processDefinitionKey(task.getWorkflowType().getProcessDefinitionKey())
        .processInstanceBusinessKey(businessKey.toString())
        .active()
        .taskDefinitionKey(task.getTaskKey())
        .singleResult()
    );

    taskToComplete.ifPresentOrElse(
        foundTask -> taskService.complete(foundTask.getId()),
        () -> {
          throw new WorkflowException(
              String.format("Active task: [%s] not found for workflow: [%s] and business key: [%s]",
                  task.name(),
                  task.getWorkflowType().name(),
                  businessKey.toString()));
        }
    );

  }

}
