package uk.co.ogauthority.pwa.service.workflow;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.model.workflow.GenericWorkflowSubject;
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
  private final RepositoryService repositoryService;
  private final TaskService taskService;

  @Autowired
  public CamundaWorkflowService(RuntimeService runtimeService,
                                TaskService taskService,
                                RepositoryService repositoryService) {
    this.taskService = taskService;
    this.runtimeService = runtimeService;
    this.repositoryService = repositoryService;
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

  public void deleteProcessAndTask(WorkflowTaskInstance workflowTaskInstance) {

    getWorkflowTask(workflowTaskInstance).ifPresentOrElse(
        foundTask -> {
            runtimeService.deleteProcessInstance(foundTask.getProcessInstanceId(), null);
            taskService.deleteTask(foundTask.getId());
        }, () -> throwTaskNotFoundException(workflowTaskInstance)
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

    // get list of current process definitions and map proc def id to proc def key in order to resolve proc def key
    // from process instance proc def id later so that we can transform proc def key into a WorkflowType value
    Map<String, String> processDefinitionIdToProcDefKeyMap = repositoryService.createProcessDefinitionQuery()
        .active()
        .latestVersion()
        .list()
        .stream()
        .collect(Collectors.toMap(ProcessDefinition::getId, ProcessDefinition::getKey));

    // query process instances to get business keys and map all queried values into a data object to return
    return runtimeService.createProcessInstanceQuery()
        .processInstanceIds(processInstanceIdTaskMap.keySet())
        .active()
        .list()
        .stream()
        .map(processInstance -> getAssignedTaskInstance(
            processDefinitionIdToProcDefKeyMap.get(processInstance.getProcessDefinitionId()),
            processInstance,
            processInstanceIdTaskMap.get(processInstance.getProcessInstanceId()),
            person))
        .collect(Collectors.toSet());

  }

  private AssignedTaskInstance getAssignedTaskInstance(String processDefinitionKey,
                                                       ProcessInstance processInstance,
                                                       Task task,
                                                       Person person) {

    var workflowType = WorkflowType.resolveFromProcessDefinitionKey(processDefinitionKey);

    var workflowTaskInstance = new WorkflowTaskInstance(
        new GenericWorkflowSubject(Integer.parseInt(processInstance.getBusinessKey()), workflowType),
        UserWorkflowTaskUtils.getTaskByWorkflowAndTaskKey(workflowType, task.getTaskDefinitionKey())
    );

    return new AssignedTaskInstance(workflowTaskInstance, person);

  }

  public Optional<PersonId> getAssignedPersonId(WorkflowTaskInstance workflowTaskInstance) {

    return getWorkflowTask(workflowTaskInstance)
        .filter(task -> task.getAssignee() != null)
        .map(task -> new PersonId(Integer.parseInt(task.getAssignee())));
    
  }

}
