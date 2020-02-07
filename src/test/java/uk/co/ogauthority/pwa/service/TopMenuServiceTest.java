package uk.co.ogauthority.pwa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.service.SystemAreaAccessService;
import uk.co.ogauthority.pwa.energyportal.service.TopMenuService;
import uk.co.ogauthority.pwa.model.TopMenuItem;

@RunWith(MockitoJUnitRunner.class)
public class TopMenuServiceTest {

  @Mock
  private SystemAreaAccessService systemAreaAccessService;

  @Mock
  private AuthenticatedUserAccount userAccount;

  private TopMenuService topMenuService;

  @Before
  public void setUp() {
    topMenuService = new TopMenuService(systemAreaAccessService);
  }

  @Test
  public void getTopMenuItems_all() {
    when(systemAreaAccessService.canAccessWorkArea(any())).thenReturn(true);
    when(systemAreaAccessService.canAccessTeamManagement(any())).thenReturn(true);

    assertThat(topMenuService.getTopMenuItems(userAccount))
        .extracting(TopMenuItem::getDisplayName)
        .containsExactly(TopMenuService.WORK_AREA_TITLE, TopMenuService.TEAM_MANAGEMENT_TITLE);
  }

  @Test
  public void getTopMenuItems_none() {
    when(systemAreaAccessService.canAccessWorkArea(any())).thenReturn(false);
    when(systemAreaAccessService.canAccessTeamManagement(any())).thenReturn(false);

    assertThat(topMenuService.getTopMenuItems(userAccount)).isEmpty();
  }

  @Test
  public void getTopMenuItems_workAreaOnly() {
    when(systemAreaAccessService.canAccessWorkArea(any())).thenReturn(true);
    when(systemAreaAccessService.canAccessTeamManagement(any())).thenReturn(false);

    assertThat(topMenuService.getTopMenuItems(userAccount))
        .extracting(TopMenuItem::getDisplayName)
        .containsExactly(TopMenuService.WORK_AREA_TITLE);
  }

  @Test
  public void getTopMenuItems_teamManagementOnly() {
    when(systemAreaAccessService.canAccessWorkArea(any())).thenReturn(false);
    when(systemAreaAccessService.canAccessTeamManagement(any())).thenReturn(true);

    assertThat(topMenuService.getTopMenuItems(userAccount))
        .extracting(TopMenuItem::getDisplayName)
        .containsExactly(TopMenuService.TEAM_MANAGEMENT_TITLE);
  }

}
