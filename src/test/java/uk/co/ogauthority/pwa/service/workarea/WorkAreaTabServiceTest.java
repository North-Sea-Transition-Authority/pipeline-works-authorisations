package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@RunWith(MockitoJUnitRunner.class)
public class WorkAreaTabServiceTest {

  private WorkAreaTabService workAreaTabService;

  private WebUserAccount wua= new WebUserAccount(1);
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(wua, Arrays.asList(PwaUserPrivilege.values()));

  @Mock
  private UserTypeService userTypeService;
  
  @Before
  public void setUp() {

    workAreaTabService = new WorkAreaTabService(userTypeService);

  }

  @Test
  public void getDefaultTabForUser_regulator() {
    when(userTypeService.getUserType(user)).thenReturn(UserType.OGA);

    var defaultTabOpt = workAreaTabService.getDefaultTabForUser(user);

    assertThat(defaultTabOpt).isPresent();

    assertThat(defaultTabOpt.get()).isEqualTo(WorkAreaTab.REGULATOR_OPEN_APPLICATIONS);

  }

  @Test
  public void getDefaultTabForUser_industry() {
    when(userTypeService.getUserType(user)).thenReturn(UserType.INDUSTRY);

    var defaultTabOpt = workAreaTabService.getDefaultTabForUser(user);

    assertThat(defaultTabOpt).isPresent();

    assertThat(defaultTabOpt.get()).isEqualTo(WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS);

  }

  @Test
  public void getDefaultTabForUser_consultee() {
    when(userTypeService.getUserType(user)).thenReturn(UserType.CONSULTEE);

    var defaultTabOpt = workAreaTabService.getDefaultTabForUser(user);

    assertThat(defaultTabOpt).isPresent();

    assertThat(defaultTabOpt.get()).isEqualTo(WorkAreaTab.OPEN_CONSULTATIONS);

  }

  @Test
  public void getDefaultTabForUser_noTabs() {
    
    var defaultTabOpt = workAreaTabService.getDefaultTabForUser(user);

    assertThat(defaultTabOpt).isEmpty();

  }

  @Test
  public void getTabsAvailableToUser_regulatorOnly() {
    when(userTypeService.getUserType(user)).thenReturn(UserType.OGA);
    var tabs = workAreaTabService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.REGULATOR_OPEN_APPLICATIONS);

  }

  @Test
  public void getTabsAvailableToUser_industryOnly() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.INDUSTRY);

    var tabs = workAreaTabService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS, WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS);

  }

  @Test
  public void getTabsAvailableToUser_consulteeOnly() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.CONSULTEE);

    var tabs = workAreaTabService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.OPEN_CONSULTATIONS);

  }


  @Test
  public void getTabsAvailableToUser_noTabs() {

    var tabs = workAreaTabService.getTabsAvailableToUser(user);

    assertThat(tabs).isEmpty();

  }

}