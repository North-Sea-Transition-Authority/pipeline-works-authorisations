package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.PwaAppProcessingTaskListService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;

@RunWith(MockitoJUnitRunner.class)
public class TasksTabContentServiceTest {

  @Mock
  private PwaAppProcessingTaskListService taskListService;

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  private TasksTabContentService taskTabContentService;

  private WebUserAccount wua;

  @Before
  public void setUp() {

    taskTabContentService = new TasksTabContentService(taskListService, applicationUpdateRequestService, pwaApplicationRedirectService);

    when(pwaApplicationRedirectService.getTaskListRoute(any())).thenReturn("#");

    wua = new WebUserAccount(1);

  }

  @Test
  public void getTabContentModelMap_tasksTab_populated() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);

    var requestView = mock(ApplicationUpdateRequestView.class);

    when(applicationUpdateRequestService.getOpenRequestView(any())).thenReturn(Optional.of(requestView));

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsOnly(
            tuple("taskListGroups", taskListGroupsList),
            tuple("industryFlag", true),
            tuple("updateRequestView", requestView),
            tuple("taskListUrl", "#")
        );

  }

  @Test
  public void getTabContentModelMap_differentTab_empty() {

    var processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.CASE_HISTORY);

    verifyNoInteractions(taskListService);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("taskListGroups", List.of()),
            tuple("industryFlag", true),
            tuple("taskListUrl", "")
        );

  }

  private PwaAppProcessingContext createContextWithPermissions(PwaAppProcessingPermission... permissions) {
    return new PwaAppProcessingContext(
        new PwaApplicationDetail(),
        wua,
        Set.of(permissions),
        null
    );
  }

}
