package uk.co.ogauthority.pwa.features.appprocessing.casemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

@ExtendWith(MockitoExtension.class)
class AppProcessingTabServiceTest {

  @Mock
  private TasksTabContentService tasksTabContentService;

  @Mock
  private CaseHistoryTabContentService caseHistoryTabContentService;

  private AppProcessingTabService tabService;

  private WebUserAccount wua;
  private AuthenticatedUserAccount authenticatedUserAccount;

  @BeforeEach
  void setUp() {

    tabService = new AppProcessingTabService(List.of(tasksTabContentService, caseHistoryTabContentService));

    wua = new WebUserAccount(1);
    authenticatedUserAccount = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  }

  @Test
  void getTabsAvailableToUser_all() {

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS,
        AppProcessingTab.CASE_HISTORY
    );

  }

  @Test
  void getTabsAvailableToUser_industryOnly() {

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of(PwaUserPrivilege.PWA_INDUSTRY));

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS
    );

  }

  @Test
  void getTabsAvailableToUser_regulatorOnly() {

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of(PwaUserPrivilege.PWA_REGULATOR));

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(
        AppProcessingTab.TASKS,
        AppProcessingTab.CASE_HISTORY
    );

  }

  @Test
  void getTabsAvailableToUser_consulteeOnly() {

    authenticatedUserAccount = new AuthenticatedUserAccount(wua, Set.of(PwaUserPrivilege.PWA_CONSULTEE));

    var tabs = tabService.getTabsAvailableToUser(authenticatedUserAccount);

    assertThat(tabs).containsExactly(AppProcessingTab.TASKS);

  }

  @Test
  void getTabContentModelMap_allTabContentRetrieved() {

    var context = new PwaAppProcessingContext(null, null, null, null, null, Set.of());

    tabService.getTabContentModelMap(context, AppProcessingTab.TASKS);

    verify(tasksTabContentService, times(1)).getTabContent(context, AppProcessingTab.TASKS);
    verify(caseHistoryTabContentService, times(1)).getTabContent(context, AppProcessingTab.TASKS);

  }



}
