package uk.co.ogauthority.pwa.integrations.camunda.external;

import static java.util.stream.Collectors.toList;

import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.HashSet;
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
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;

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

  public Set<WorkflowTaskInstance> getAllActiveWorkflowTasks(WorkflowSubject workflowSubject) {

    var processInstanceDefinitionKey = workflowSubject.getWorkflowType().getProcessDefinitionKey();
    var businessKey = String.valueOf(workflowSubject.getBusinessKey());

    return taskService.createTaskQuery()
        .processDefinitionKey(processInstanceDefinitionKey)
        .processInstanceBusinessKey(businessKey)
        .active()
        .list()
        .stream()
        .map(task -> getWorkFlowInstance(processInstanceDefinitionKey, businessKey, task))
        .collect(Collectors.toUnmodifiableSet());
  }


  public Set<WorkflowBusinessKey> filterBusinessKeysByWorkflowTypeAndActiveTasksContains(WorkflowType workflowType,
                                                                                         Set<WorkflowBusinessKey> workflowBusinessKeys,
                                                                                         Set<UserWorkflowTask> userWorkflowTasks) {

    // task query `processInstanceBusinessKeyIn` resolves to true when no business keys, exit early
    if (workflowBusinessKeys.isEmpty()) {
      return Set.of();
    }

    var processInstanceDefinitionKey = workflowType.getProcessDefinitionKey();

    var businessKeysArray = workflowBusinessKeys.stream()
        .map(WorkflowBusinessKey::getValue)
        .collect(toList())
        .toArray(String[]::new);

    var taskKeyArray = userWorkflowTasks.stream()
        .map(UserWorkflowTask::getTaskKey)
        .collect(toList())
        .toArray(String[]::new);

    var processInstanceIds = taskService.createTaskQuery()
        .processDefinitionKey(processInstanceDefinitionKey)
        .processInstanceBusinessKeyIn(businessKeysArray)
        .taskDefinitionKeyIn(taskKeyArray)
        .active()
        .list()
        .stream()
        .map(Task::getProcessInstanceId)
        .collect(Collectors.toUnmodifiableSet());

    if (processInstanceIds.isEmpty()) {
      return Collections.emptySet();
    }

    return runtimeService.createProcessInstanceQuery()
        .active()
        .processInstanceIds(processInstanceIds)
        .list()
        .stream()
        .map(pi -> WorkflowBusinessKey.from(pi.getBusinessKey()))
        .collect(Collectors.toSet());
  }

  @Transactional
  public void completeTask(WorkflowTaskInstance workflowTaskInstance) {

    getWorkflowTask(workflowTaskInstance).ifPresentOrElse(
        foundTask -> taskService.complete(foundTask.getId()),
        () -> throwTaskNotFoundException(workflowTaskInstance)
    );

  }

  @Transactional
  public void deleteProcessAndTask(WorkflowTaskInstance workflowTaskInstance) {

    getWorkflowTask(workflowTaskInstance).ifPresentOrElse(
        foundTask -> {
          runtimeService.deleteProcessInstance(foundTask.getProcessInstanceId(), null);
          taskService.deleteTask(foundTask.getId());
        }, () -> throwTaskNotFoundException(workflowTaskInstance)
    );

  }

  @Transactional
  public void deleteProcessInstanceAndThenTasks(WorkflowSubject workflowSubject) {

    var workflowTaskInstances = getAllActiveWorkflowTasks(workflowSubject);
    var tasks = getTasksFromWorkflowTaskInstances(workflowTaskInstances);

    var processInstance = getProcessInstance(workflowSubject)
        .orElseThrow(() -> new NullPointerException("Process instance not found for " + workflowSubject.getDebugString()));
    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), null);

    tasks.forEach(task -> taskService.deleteTask(task.getId()));
  }


  public Set<Task> getTasksFromWorkflowTaskInstances(Set<WorkflowTaskInstance> workflowTaskInstances) {

    Set<Task> tasks = new HashSet<>();
    for (var workflowTaskInstance : workflowTaskInstances) {
      getWorkflowTask(workflowTaskInstance).ifPresentOrElse(
          tasks::add, () -> throwTaskNotFoundException(workflowTaskInstance)
      );
    }
    return tasks;
  }

  @Transactional
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

    var workflowTaskInstance = getWorkFlowInstance(processDefinitionKey, processInstance.getBusinessKey(), task);

    return new AssignedTaskInstance(workflowTaskInstance, person);

  }

  private WorkflowTaskInstance getWorkFlowInstance(String processDefinitionKey,
                                                   String businessKey,
                                                   Task task) {
    var workflowType = WorkflowType.resolveFromProcessDefinitionKey(processDefinitionKey);

    return new WorkflowTaskInstance(
        new GenericWorkflowSubject(Integer.parseInt(businessKey), workflowType),
        UserWorkflowTaskUtils.getTaskByWorkflowAndTaskKey(workflowType, task.getTaskDefinitionKey())
    );
  }

  public Optional<PersonId> getAssignedPersonId(WorkflowTaskInstance workflowTaskInstance) {

    return getWorkflowTask(workflowTaskInstance)
        .filter(task -> task.getAssignee() != null)
        .map(task -> new PersonId(Integer.parseInt(task.getAssignee())));

  }


  @Transactional
  public void triggerMessageEvent(WorkflowSubject workflowSubject,
                                  String messageEventName) {

    ProcessInstance processInstance = getProcessInstanceOrError(workflowSubject);
    try {
      runtimeService.createMessageCorrelation(messageEventName)
          .processInstanceId(processInstance.getProcessInstanceId())
          .correlateWithResult();

    } catch (Exception e) {
      throw new WorkflowException(
          String.format("Failed to correlate message [%s] for workflowSubject. \n %s ", messageEventName,
              workflowSubject.getDebugString()),
          e
      );
    }

  }

  public Optional<ProcessInstance> getProcessInstance(WorkflowSubject workflowSubject) {
    return Optional.ofNullable(runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(workflowSubject.getWorkflowType().getProcessDefinitionKey())
        .processInstanceBusinessKey(String.valueOf(workflowSubject.getBusinessKey()))
        .singleResult());
  }

  private ProcessInstance getProcessInstanceOrError(WorkflowSubject workflowSubject) {
    return getProcessInstance(workflowSubject)
        .orElseThrow(
            () -> new WorkflowException("could not find process instance for subject: " + workflowSubject.toString()));
  }


  @Transactional
  public void setWorkflowProperty(WorkflowSubject workflowSubject, WorkflowProperty workflowProperty) {
    var processInstance = getProcessInstanceOrError(workflowSubject);
    runtimeService.setVariable(processInstance.getId(), workflowProperty.getPropertyName(),
        workflowProperty.getPropertyValue());

  }


}
