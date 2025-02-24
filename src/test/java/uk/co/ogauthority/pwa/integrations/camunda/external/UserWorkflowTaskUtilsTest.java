package uk.co.ogauthority.pwa.integrations.camunda.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;

@RunWith(SpringJUnit4ClassRunner.class)
class UserWorkflowTaskUtilsTest {

  @Test
  void getTaskByWorkflowAndTaskKey_pwaApplication() {

    Stream.of(PwaApplicationWorkflowTask.values()).forEach(task -> {

      var resolvedTask = UserWorkflowTaskUtils.getTaskByWorkflowAndTaskKey(WorkflowType.PWA_APPLICATION, task.getTaskKey());

      assertThat(resolvedTask).isEqualTo(task);

    });

  }

  @Test
  void getTaskByWorkflowAndTaskKey_pwaApplication_invalid() {
    assertThrows(RuntimeException.class, () ->
      UserWorkflowTaskUtils.getTaskByWorkflowAndTaskKey(WorkflowType.PWA_APPLICATION, "myInvalidTask"));
  }

}
