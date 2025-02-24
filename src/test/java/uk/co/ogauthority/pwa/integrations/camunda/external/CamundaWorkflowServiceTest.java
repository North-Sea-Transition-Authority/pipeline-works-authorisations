package uk.co.ogauthority.pwa.integrations.camunda.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.InitialReviewPaymentDecision;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
@Transactional
class CamundaWorkflowServiceTest {

  @Autowired
  private CamundaWorkflowService camundaWorkflowService;

  @Autowired
  private TaskService taskService;

  private PwaApplicationDetail applicationDetail;
  private PwaApplication application;
  private ConsultationRequest consultationRequest;

  private WebUserAccount webUserAccount = new WebUserAccount(1);

  @BeforeEach
  void setUp() {
    applicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);
    application = applicationDetail.getPwaApplication();
    consultationRequest = new ConsultationRequest();
    consultationRequest.setId(1);
  }

  @Test
  void start() {

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.setWorkflowProperty(application, PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION);

    assertThat(taskService
        .createTaskQuery()
        .processDefinitionKey(WorkflowType.PWA_APPLICATION.getProcessDefinitionKey())
        .processInstanceBusinessKey("1")
        .active()
        .taskDefinitionKey(PwaApplicationWorkflowTask.PREPARE_APPLICATION.getTaskKey())
        .singleResult()).isNotNull();

  }

  @Test
  void completeTask_singleTask() {

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.setWorkflowProperty(application, PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION));

    assertThat(taskService
        .createTaskQuery()
        .processDefinitionKey(WorkflowType.PWA_APPLICATION.getProcessDefinitionKey())
        .processInstanceBusinessKey("1")
        .active()
        .taskDefinitionKey(PwaApplicationWorkflowTask.PREPARE_APPLICATION.getTaskKey())
        .singleResult()).isNull();

  }

  @Test
  void completeTask_multipleTasks_error() {
    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.setWorkflowProperty(application, PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION));
    camundaWorkflowService.triggerMessageEvent(
          application, PwaApplicationWorkflowMessageEvents.UPDATE_APPLICATION_REQUEST.getMessageEventName());
    camundaWorkflowService.triggerMessageEvent(
          application, PwaApplicationWorkflowMessageEvents.UPDATE_APPLICATION_REQUEST.getMessageEventName());
    assertThat(taskService
          .createTaskQuery()
          .processDefinitionKey(WorkflowType.PWA_APPLICATION.getProcessDefinitionKey())
          .processInstanceBusinessKey("1")
          .active()
          .taskDefinitionKey(PwaApplicationWorkflowTask.UPDATE_APPLICATION.getTaskKey())
          .list()
          .size()).isEqualTo(2);
    assertThrows(ProcessEngineException.class, () ->

      camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.UPDATE_APPLICATION)));

  }

  @Test
  void completeTask_doesntExist() {
    camundaWorkflowService.startWorkflow(application);
    assertThrows(WorkflowException.class, () ->
      camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW)));

  }

  @Test
  void deleteProcessAndTask() {

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.deleteProcessAndTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION));

    assertThat(taskService
        .createTaskQuery()
        .processDefinitionKey(WorkflowType.PWA_APPLICATION.getProcessDefinitionKey())
        .processInstanceBusinessKey("1")
        .active()
        .taskDefinitionKey(PwaApplicationWorkflowTask.PREPARE_APPLICATION.getTaskKey())
        .singleResult()).isNull();

  }

  @Test
  void deleteProcessAndTask_doesntExist() {
    camundaWorkflowService.startWorkflow(application);
    assertThrows(WorkflowException.class, () ->
      camundaWorkflowService.deleteProcessAndTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW)));

  }

  @Test
  void deleteProcessInstanceAndThenTasks_processInstanceNotFound() {
    assertThrows(NullPointerException.class, () ->
      camundaWorkflowService.deleteProcessInstanceAndThenTasks(application));
  }

  @Test
  void getTasksFromWorkflowTaskInstances() {
    camundaWorkflowService.startWorkflow(application);
    var taskInstance = new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION);
    var tasks = camundaWorkflowService.getTasksFromWorkflowTaskInstances(Set.of(taskInstance));
    assertThat(tasks).isNotEmpty();
  }

  @Test
  void assignTaskToUser_valid() {

    var person = new Person(111, null, null, null, null);

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.setWorkflowProperty(application, PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION));
    camundaWorkflowService.setWorkflowProperty(application, InitialReviewPaymentDecision.PAYMENT_WAIVED.getPwaApplicationInitialReviewResult());
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW));

    camundaWorkflowService.assignTaskToUser(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW), person);

    assertThat(taskService
        .createTaskQuery()
        .processDefinitionKey(WorkflowType.PWA_APPLICATION.getProcessDefinitionKey())
        .processInstanceBusinessKey("1")
        .active()
        .taskDefinitionKey(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW.getTaskKey())
        .singleResult()
        .getAssignee()).isEqualTo("111");

  }

  @Test
  void assignTaskToUser_noTask() {
    var person = new Person(111, null, null, null, null);
    camundaWorkflowService.startWorkflow(application);
    assertThrows(WorkflowException.class, () ->
      camundaWorkflowService.assignTaskToUser(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW), person));

  }

  @Test
  void getAssignedTasks() {

    var person = new Person(11, null, null, null, null);
    webUserAccount = new WebUserAccount(1, person);

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.setWorkflowProperty(application, PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION));
    camundaWorkflowService.setWorkflowProperty(application, InitialReviewPaymentDecision.PAYMENT_WAIVED.getPwaApplicationInitialReviewResult());
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW));

    camundaWorkflowService.assignTaskToUser(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW), person);

    var assignedTasks = camundaWorkflowService.getAssignedTasks(person);

    assertThat(assignedTasks.size()).isEqualTo(1);

    var task = assignedTasks.iterator().next();

    assertThat(task.getBusinessKey()).isEqualTo(application.getBusinessKey());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW.getTaskKey());
    assertThat(task.getWorkflowType()).isEqualTo(WorkflowType.PWA_APPLICATION);
    assertThat(task.getAssignee()).isEqualTo(person);

  }

  @Test
  void getAssignedTasks_noTasks() {

    var person = new Person(2, null, null, null, null);
    assertThat(camundaWorkflowService.getAssignedTasks(person)).isEmpty();

  }

  @Test
  void getAssignedPersonId_taskExists_present() {

    var person = new Person(11, null, null, null, null);

    camundaWorkflowService.startWorkflow(consultationRequest);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(consultationRequest,
        PwaApplicationConsultationWorkflowTask.ALLOCATION));

    var responseTaskInstance = new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE);

    camundaWorkflowService.assignTaskToUser(responseTaskInstance, person);

    var assignedPersonId = camundaWorkflowService.getAssignedPersonId(responseTaskInstance);

    assertThat(assignedPersonId).isPresent();

    assertThat(assignedPersonId.get()).isEqualTo(person.getId());

  }

  @Test
  void getAssignedPersonId_taskExists_empty() {

    camundaWorkflowService.startWorkflow(consultationRequest);

    var assignedPersonId = camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION));

    assertThat(assignedPersonId).isEmpty();

  }

  @Test
  void getAssignedPersonId_noTask() {

    var assignedPersonId = camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION));

    assertThat(assignedPersonId).isEmpty();

  }

  @Test
  void triggerMessageEvent_noMatchingEvent(){
    camundaWorkflowService.startWorkflow(consultationRequest);
    assertThrows(WorkflowException.class, () ->
      camundaWorkflowService.triggerMessageEvent(consultationRequest, "SOME_EVENT"));
  }

  @Test
  void triggerMessageEvent_matchingEvent(){
    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.setWorkflowProperty(application, PwaApplicationSubmitResult.SUBMIT_PREPARED_APPLICATION);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION));
    camundaWorkflowService.setWorkflowProperty(application, InitialReviewPaymentDecision.PAYMENT_WAIVED.getPwaApplicationInitialReviewResult());
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW));
    camundaWorkflowService.triggerMessageEvent(application, PwaApplicationWorkflowMessageEvents.UPDATE_APPLICATION_REQUEST.getMessageEventName());

    assertThat(camundaWorkflowService.getAllActiveWorkflowTasks(application)).anySatisfy(workflowTaskInstance ->
      assertThat(workflowTaskInstance.getTaskKey()).isEqualTo(PwaApplicationWorkflowTask.UPDATE_APPLICATION.getTaskKey()));

  }

  @Test
  void filterBusinessKeysByWorkflowTypeAndActiveTasksContains_filterTaskIsActive(){

    camundaWorkflowService.startWorkflow(application);

    var filteredBusinesskeys = camundaWorkflowService.filterBusinessKeysByWorkflowTypeAndActiveTasksContains(
        WorkflowType.PWA_APPLICATION,
        Set.of(WorkflowBusinessKey.from(application.getBusinessKey())),
        Set.of(PwaApplicationWorkflowTask.PREPARE_APPLICATION)
    );

    assertThat(filteredBusinesskeys).containsExactly(WorkflowBusinessKey.from(application.getBusinessKey()));

  }

  @Test
  void filterBusinessKeysByWorkflowTypeAndActiveTasksContains_noBusinessKeysPassed_emptySetReturned(){

    camundaWorkflowService.startWorkflow(application);

    var filteredBusinesskeys = camundaWorkflowService.filterBusinessKeysByWorkflowTypeAndActiveTasksContains(
        WorkflowType.PWA_APPLICATION,
        Set.of(),
        Set.of(PwaApplicationWorkflowTask.PREPARE_APPLICATION)
    );

    assertThat(filteredBusinesskeys).isEmpty();

  }

  @Test
  void filterBusinessKeysByWorkflowTypeAndActiveTasksContains_filterTaskDoesNotExist(){

    camundaWorkflowService.startWorkflow(application);

    var filteredBusinesskeys = camundaWorkflowService.filterBusinessKeysByWorkflowTypeAndActiveTasksContains(
        WorkflowType.PWA_APPLICATION,
        Set.of(WorkflowBusinessKey.from(application.getBusinessKey())),
        Set.of(PwaApplicationWorkflowTask.AWAIT_FEEDBACK)
    );

    assertThat(filteredBusinesskeys).isEmpty();
  }

}
