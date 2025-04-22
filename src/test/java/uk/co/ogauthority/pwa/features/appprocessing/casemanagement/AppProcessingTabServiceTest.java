package uk.co.ogauthority.pwa.features.appprocessing.casemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class AppProcessingTabServiceTest {

  @Mock
  private TasksTabContentService tasksTabContentService;

  @Mock
  private CaseHistoryTabContentService caseHistoryTabContentService;

  @Mock
  private TeamQueryService teamQueryService;

  private AppProcessingTabService tabService;

  private WebUserAccount wua;
  private AuthenticatedUserAccount authenticatedUserAccount;

  @BeforeEach
  void setUp() {

    tabService = new AppProcessingTabService(List.of(tasksTabContentService, caseHistoryTabContentService), teamQueryService);

    wua = new WebUserAccount(1);
    authenticatedUserAccount = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  }

  @Test
  void getTabsAvailableToUser_all() {

    when(teamQueryService.userIsMemberOfStaticTeam(1L, TeamType.REGULATOR)).thenReturn(true);
    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS,
        AppProcessingTab.CASE_HISTORY
    );

  }

  @Test
  void getTabsAvailableToUser_industryOnly() {

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of());

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS
    );

  }

  @Test
  void getTabsAvailableToUser_regulatorOnly() {

    when(teamQueryService.userIsMemberOfStaticTeam(1L, TeamType.REGULATOR)).thenReturn(true);

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of());

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS,
        AppProcessingTab.CASE_HISTORY
    );

  }

  @Test
  void getTabsAvailableToUser_consulteeOnly() {

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of());

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(AppProcessingTab.TASKS);

  }

  @Test
  void getTabContentModelMap_allTabContentRetrieved() {

    var context = new PwaAppProcessingContext(null, null, null, null, null, Set.of());

    tabService.getTabContentModelMap(context, AppProcessingTab.TASKS);

    verify(tasksTabContentService).getTabContent(context, AppProcessingTab.TASKS);
    verify(caseHistoryTabContentService).getTabContent(context, AppProcessingTab.TASKS);

  }



}
