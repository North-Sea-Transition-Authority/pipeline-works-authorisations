package uk.co.ogauthority.pwa.service.workflow;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.model.workflow.PwaApplicationWorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workflow.task.AssignedTaskInstance;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.util.workflow.UserWorkflowTaskUtils;

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

  public Set<AssignedTaskInstance> getAssignedTasks(Person person) {

    // get a map of process instance id to task for tasks assigned to our person
    Map<String, Task> processInstanceIdTaskMap = taskService.createTaskQuery()
        .taskAssignee(String.valueOf(person.getId().asInt()))
        .active()
        .list()
        .stream()
        .collect(Collectors.toMap(Task::getProcessInstanceId, task -> task));

    if (processInstanceIdTaskMap.isEmpty()) {
      return Set.of();
    }

    // query process instances to get business keys and map all queried values into a data object to return
    return runtimeService.createProcessInstanceQuery()
        .processInstanceIds(processInstanceIdTaskMap.keySet())
        .active()
        .list()
        .stream()
        .map(processInstance -> getAssignedTaskInstance(
            processInstance,
            processInstanceIdTaskMap.get(processInstance.getProcessInstanceId()),
            person))
        .collect(Collectors.toSet());

  }

  private AssignedTaskInstance getAssignedTaskInstance(ProcessInstance processInstance, Task task, Person person) {

    var workflowType = WorkflowType.resolveFromProcessDefinitionKey(isolateProcessDefinitionKey(processInstance.getProcessDefinitionId()));

    var workflowTaskInstance = new WorkflowTaskInstance(
        new PwaApplicationWorkflowSubject(Integer.parseInt(processInstance.getBusinessKey()), workflowType),
        UserWorkflowTaskUtils.getTaskByWorkflowAndTaskKey(workflowType, task.getTaskDefinitionKey())
    );

    return new AssignedTaskInstance(workflowTaskInstance, person);

  }

  private String isolateProcessDefinitionKey(String procDefId) {
    return procDefId.substring(0, procDefId.indexOf(":"));
  }

}
