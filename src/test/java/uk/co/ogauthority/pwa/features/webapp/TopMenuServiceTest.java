package uk.co.ogauthority.pwa.features.webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.TopMenuItem;

@ExtendWith(MockitoExtension.class)
class TopMenuServiceTest {

  @Mock
  private SystemAreaAccessService systemAreaAccessService;

  @Mock
  private AuthenticatedUserAccount userAccount;

  private TopMenuService topMenuService;

  @BeforeEach
  void setUp() {
    topMenuService = new TopMenuService(systemAreaAccessService);
  }

  @Test
  void getTopMenuItems_all() {
    when(systemAreaAccessService.canAccessWorkArea(any())).thenReturn(true);
    when(systemAreaAccessService.canAccessTeamManagement(any())).thenReturn(true);

    assertThat(topMenuService.getTopMenuItems(userAccount))
        .extracting(TopMenuItem::getDisplayName)
        .containsExactly(TopMenuService.WORK_AREA_TITLE, TopMenuService.TEAM_MANAGEMENT_TITLE);
  }

  @Test
  void getTopMenuItems_none() {
    when(systemAreaAccessService.canAccessWorkArea(any())).thenReturn(false);
    when(systemAreaAccessService.canAccessTeamManagement(any())).thenReturn(false);

    assertThat(topMenuService.getTopMenuItems(userAccount)).isEmpty();
  }

  @Test
  void getTopMenuItems_workAreaOnly() {
    when(systemAreaAccessService.canAccessWorkArea(any())).thenReturn(true);
    when(systemAreaAccessService.canAccessTeamManagement(any())).thenReturn(false);

    assertThat(topMenuService.getTopMenuItems(userAccount))
        .extracting(TopMenuItem::getDisplayName)
        .containsExactly(TopMenuService.WORK_AREA_TITLE);
  }

  @Test
  void getTopMenuItems_teamManagementOnly() {
    when(systemAreaAccessService.canAccessWorkArea(any())).thenReturn(false);
    when(systemAreaAccessService.canAccessTeamManagement(any())).thenReturn(true);

    assertThat(topMenuService.getTopMenuItems(userAccount))
        .extracting(TopMenuItem::getDisplayName)
        .containsExactly(TopMenuService.TEAM_MANAGEMENT_TITLE);
  }

  @Test
  void getTopMenuItems_consentSearchOnly() {
    when(systemAreaAccessService.canAccessConsentSearch(any())).thenReturn(true);

    assertThat(topMenuService.getTopMenuItems(userAccount))
        .extracting(TopMenuItem::getDisplayName)
        .containsExactly(TopMenuService.CONSENT_SEARCH_TITLE);
  }

  @Test
  void getTopMenuItems_caseReassignmentOnly() {
    when(systemAreaAccessService.isManagement(any())).thenReturn(true);

    assertThat(topMenuService.getTopMenuItems(userAccount))
        .extracting(TopMenuItem::getDisplayName)
        .containsExactly(TopMenuService.REASSIGN_APPLICATIONS_TITLE);
  }

  @Test
  void getTopMenuItems_templateClauseManagementOnly() {

    when(systemAreaAccessService.canAccessTemplateClauseManagement(any())).thenReturn(true);

    assertThat(topMenuService.getTopMenuItems(userAccount))
        .extracting(TopMenuItem::getDisplayName)
        .containsExactly(TopMenuService.TEMPLATE_CLAUSE_MANAGE_TITLE);

  }

}
