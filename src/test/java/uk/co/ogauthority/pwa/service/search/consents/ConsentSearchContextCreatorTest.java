package uk.co.ogauthority.pwa.service.search.consents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsentSearchContextCreatorTest {

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private TeamService teamService;

  private ConsentSearchContextCreator consentSearchContextCreator;

  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of());

    consentSearchContextCreator = new ConsentSearchContextCreator(userTypeService, teamService);

  }

  @Test
  public void createContext_industryUser() {

    int orgGroupId = 1;
    var orgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(orgGroupId, "OG", "OG");

    var orgTeam = mock(PwaOrganisationTeam.class);
    when(orgTeam.getPortalOrganisationGroup()).thenReturn(orgGroup);

    when(userTypeService.getUserType(user)).thenReturn(UserType.INDUSTRY);
    when(teamService.getOrganisationTeamListIfPersonInRole(eq(user.getLinkedPerson()), any())).thenReturn(List.of(orgTeam));

    var context = consentSearchContextCreator.createContext(user);

    assertThat(context.getUser()).isEqualTo(user);
    assertThat(context.getUserType()).isEqualTo(UserType.INDUSTRY);
    assertThat(context.getOrgGroupIdsUserInTeamFor()).containsExactly(orgGroupId);

  }

  @Test
  public void createContext_regulatorUser() {

    when(userTypeService.getUserType(user)).thenReturn(UserType.OGA);

    var context = consentSearchContextCreator.createContext(user);
    verifyNoInteractions(teamService);

    assertThat(context.getUser()).isEqualTo(user);
    assertThat(context.getUserType()).isEqualTo(UserType.OGA);
    assertThat(context.getOrgGroupIdsUserInTeamFor()).isEmpty();

  }

}