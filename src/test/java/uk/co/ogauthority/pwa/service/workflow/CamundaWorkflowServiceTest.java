package uk.co.ogauthority.pwa.service.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.TaskService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
@Transactional
public class CamundaWorkflowServiceTest {

  @Autowired
  private CamundaWorkflowService camundaWorkflowService;

  @Autowired
  private TaskService taskService;

  private PwaApplication application;
  private ConsultationRequest consultationRequest;

  @Before
  public void setUp() {
    application = new PwaApplication();
    application.setId(1);
    consultationRequest = new ConsultationRequest();
    consultationRequest.setId(1);
  }

  @Test
  public void start() {

    camundaWorkflowService.startWorkflow(application);

    assertThat(taskService
        .createTaskQuery()
        .processDefinitionKey(WorkflowType.PWA_APPLICATION.getProcessDefinitionKey())
        .processInstanceBusinessKey("1")
        .active()
        .taskDefinitionKey(PwaApplicationWorkflowTask.PREPARE_APPLICATION.getTaskKey())
        .singleResult()).isNotNull();

  }

  @Test
  public void completeTask() {

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION));

    assertThat(taskService
        .createTaskQuery()
        .processDefinitionKey(WorkflowType.PWA_APPLICATION.getProcessDefinitionKey())
        .processInstanceBusinessKey("1")
        .active()
        .taskDefinitionKey(PwaApplicationWorkflowTask.PREPARE_APPLICATION.getTaskKey())
        .singleResult()).isNull();

  }

  @Test(expected = WorkflowException.class)
  public void completeTask_doesntExist() {

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.APPLICATION_REVIEW));

  }

  @Test
  public void assignTaskToUser_valid() {

    var person = new Person(111, null, null, null, null);

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION));
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

  @Test(expected = WorkflowException.class)
  public void assignTaskToUser_noTask() {

    var person = new Person(111, null, null, null, null);

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.assignTaskToUser(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW), person);

  }

  @Test
  public void getAssignedTasks() {

    var person = new Person(11, null, null, null, null);

    camundaWorkflowService.startWorkflow(application);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.PREPARE_APPLICATION));
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
  public void getAssignedTasks_noTasks() {

    var person = new Person(2, null, null, null, null);
    assertThat(camundaWorkflowService.getAssignedTasks(person)).isEmpty();

  }

  @Test
  public void getAssignedPersonId_taskExists_present() {

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
  public void getAssignedPersonId_taskExists_empty() {

    camundaWorkflowService.startWorkflow(consultationRequest);

    var assignedPersonId = camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION));

    assertThat(assignedPersonId).isEmpty();

  }

  @Test
  public void getAssignedPersonId_noTask() {

    var assignedPersonId = camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION));

    assertThat(assignedPersonId).isEmpty();

  }

}
