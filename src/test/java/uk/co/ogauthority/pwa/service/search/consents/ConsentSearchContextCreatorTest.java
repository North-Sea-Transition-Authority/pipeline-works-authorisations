package uk.co.ogauthority.pwa.service.search.consents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@ExtendWith(MockitoExtension.class)
class ConsentSearchContextCreatorTest {

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private TeamService teamService;

  private ConsentSearchContextCreator consentSearchContextCreator;

  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of());

    consentSearchContextCreator = new ConsentSearchContextCreator(userTypeService, teamService);

  }

  @Test
  void createContext_industryUser() {

    int orgGroupId = 1;
    var orgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(orgGroupId, "OG", "OG");

    var orgTeam = mock(PwaOrganisationTeam.class);
    when(orgTeam.getPortalOrganisationGroup()).thenReturn(orgGroup);

    when(userTypeService.getPriorityUserType(user)).thenReturn(UserType.INDUSTRY);
    when(teamService.getOrganisationTeamListIfPersonInRole(eq(user.getLinkedPerson()), any())).thenReturn(List.of(orgTeam));

    var context = consentSearchContextCreator.createContext(user);

    assertThat(context.getUser()).isEqualTo(user);
    assertThat(context.getUserType()).isEqualTo(UserType.INDUSTRY);
    assertThat(context.getOrgGroupIdsUserInTeamFor()).containsExactly(orgGroupId);

  }

  @Test
  void createContext_regulatorUser() {

    when(userTypeService.getPriorityUserType(user)).thenReturn(UserType.OGA);

    var context = consentSearchContextCreator.createContext(user);
    verifyNoInteractions(teamService);

    assertThat(context.getUser()).isEqualTo(user);
    assertThat(context.getUserType()).isEqualTo(UserType.OGA);
    assertThat(context.getOrgGroupIdsUserInTeamFor()).isEmpty();

  }

}