package uk.co.ogauthority.pwa.service.appprocessing.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class AcceptApplicationServiceTest {

  private AcceptApplicationService acceptApplicationService;

  @Before
  public void setUp() {
    acceptApplicationService = new AcceptApplicationService();
  }

  @Test
  public void canShowInTaskList_acceptApplicationPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.ACCEPT_APPLICATION), null, null);

    boolean canShow = acceptApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null);

    boolean canShow = acceptApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = acceptApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_acceptApplicationNotCompleted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null);

    var taskListEntry = acceptApplicationService.getTaskListEntry(PwaAppProcessingTask.ACCEPT_APPLICATION, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.ACCEPT_APPLICATION.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.ACCEPT_APPLICATION.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskStatus.NOT_COMPLETED);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

}
