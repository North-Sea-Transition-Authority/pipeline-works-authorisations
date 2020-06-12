package uk.co.ogauthority.pwa.util.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserWorkflowTaskUtilsTest {

  @Test
  public void getTaskByWorkflowAndTaskKey_pwaApplication() {

    Stream.of(PwaApplicationWorkflowTask.values()).forEach(task -> {

      var resolvedTask = UserWorkflowTaskUtils.getTaskByWorkflowAndTaskKey(WorkflowType.PWA_APPLICATION, task.getTaskKey());

      assertThat(resolvedTask).isEqualTo(task);

    });

  }

  @Test(expected = RuntimeException.class)
  public void getTaskByWorkflowAndTaskKey_pwaApplication_invalid() {
    UserWorkflowTaskUtils.getTaskByWorkflowAndTaskKey(WorkflowType.PWA_APPLICATION, "myInvalidTask");
  }

}
