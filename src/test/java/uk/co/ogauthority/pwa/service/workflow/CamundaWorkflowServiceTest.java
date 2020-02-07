package uk.co.ogauthority.pwa.service.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;

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

  @Test
  public void start() {

    camundaWorkflowService.startWorkflow(WorkflowType.PWA_APPLICATION, 1);

    assertThat(taskService
        .createTaskQuery()
        .processDefinitionKey(WorkflowType.PWA_APPLICATION.getProcessDefinitionKey())
        .processInstanceBusinessKey("1")
        .active()
        .taskDefinitionKey(UserWorkflowTask.PREPARE_APPLICATION.getTaskKey())
        .singleResult()).isNotNull();

  }

  @Test
  public void completeTask() {

    camundaWorkflowService.startWorkflow(WorkflowType.PWA_APPLICATION, 1);
    camundaWorkflowService.completeTask(1, UserWorkflowTask.PREPARE_APPLICATION);

    assertThat(taskService
        .createTaskQuery()
        .processDefinitionKey(WorkflowType.PWA_APPLICATION.getProcessDefinitionKey())
        .processInstanceBusinessKey("1")
        .active()
        .taskDefinitionKey(UserWorkflowTask.PREPARE_APPLICATION.getTaskKey())
        .singleResult()).isNull();

  }

  @Test(expected = WorkflowException.class)
  public void completeTask_doesntExist() {

    camundaWorkflowService.startWorkflow(WorkflowType.PWA_APPLICATION, 1);
    camundaWorkflowService.completeTask(1, UserWorkflowTask.APPLICATION_REVIEW);

  }

}
