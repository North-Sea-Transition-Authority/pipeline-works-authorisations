package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;

@RunWith(MockitoJUnitRunner.class)
public class AppProcessingTabServiceTest {

  @Mock
  private TasksTabContentService tasksTabContentService;

  @Mock
  private CaseHistoryTabContentService caseHistoryTabContentService;

  private AppProcessingTabService tabService;

  private WebUserAccount wua;
  private AuthenticatedUserAccount authenticatedUserAccount;

  @Before
  public void setUp() {

    tabService = new AppProcessingTabService(List.of(tasksTabContentService, caseHistoryTabContentService));

    wua = new WebUserAccount(1);
    authenticatedUserAccount = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  }

  @Test
  public void getTabsAvailableToUser_all() {

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS,
        AppProcessingTab.CASE_HISTORY
    );

  }

  @Test
  public void getTabsAvailableToUser_industryOnly() {

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of(PwaUserPrivilege.PWA_INDUSTRY));

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS
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

    assertThat(tabs).containsExactly(AppProcessingTab.TASKS);

  }

  @Test
  public void getTabContentModelMap_allTabContentRetrieved() {

    var context = new PwaAppProcessingContext(null, null, null, null, null, Set.of());

    tabService.getTabContentModelMap(context, AppProcessingTab.TASKS);

    verify(tasksTabContentService, times(1)).getTabContent(context, AppProcessingTab.TASKS);
    verify(caseHistoryTabContentService, times(1)).getTabContent(context, AppProcessingTab.TASKS);

  }



}
