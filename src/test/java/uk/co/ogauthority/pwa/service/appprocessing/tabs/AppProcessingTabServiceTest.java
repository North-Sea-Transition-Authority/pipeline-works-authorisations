package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.PwaAppProcessingTaskListService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;

@RunWith(MockitoJUnitRunner.class)
public class AppProcessingTabServiceTest {

  @Mock
  private PwaAppProcessingTaskListService taskListService;

  private AppProcessingTabService tabService;

  private WebUserAccount wua;
  private AuthenticatedUserAccount authenticatedUserAccount;

  @Before
  public void setUp() {

    tabService = new AppProcessingTabService(taskListService);

    wua = new WebUserAccount(1);
    authenticatedUserAccount = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  }

  @Test
  public void getTabsAvailableToUser_all() {

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS,
        AppProcessingTab.CASE_HISTORY,
        AppProcessingTab.FIRS
    );

  }

  @Test
  public void getTabsAvailableToUser_industryOnly() {

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of(PwaUserPrivilege.PWA_INDUSTRY));

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS,
        AppProcessingTab.FIRS
    );

  }

  @Test
  public void getTabsAvailableToUser_regulatorOnly() {

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of(PwaUserPrivilege.PWA_REGULATOR));

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS,
        AppProcessingTab.CASE_HISTORY
    );

  }

  @Test
  public void getTabsAvailableToUser_consulteeOnly() {

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of(PwaUserPrivilege.PWA_CONSULTEE));

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS,
        AppProcessingTab.CASE_HISTORY
    );

  }

  @Test
  public void getTabContentModelMap_tasksTab() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);

    var modelMap = tabService.getTabContentModelMap(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
          tuple("taskListGroups", taskListGroupsList),
          tuple("industryFlag", true)
        );

  }

  @Test
  public void getTabContentModelMap_caseHistoryTab() {

    var processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT);

    var modelMap = tabService.getTabContentModelMap(processingContext, AppProcessingTab.CASE_HISTORY);

    verify(taskListService, times(0)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("taskListGroups", List.of()),
            tuple("industryFlag", false)
        );

  }

  @Test
  public void getTabContentModelMap_firsTab() {

    var processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT);

    var modelMap = tabService.getTabContentModelMap(processingContext, AppProcessingTab.FIRS);

    verify(taskListService, times(0)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("taskListGroups", List.of()),
            tuple("industryFlag", false)
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
