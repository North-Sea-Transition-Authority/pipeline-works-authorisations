package uk.co.ogauthority.pwa.service.search.consents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ConsentSearchContextCreatorTest {

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  @InjectMocks
  private ConsentSearchContextCreator consentSearchContextCreator;

  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of());
  }

  @Test
  void createContext_industryUser() {

    int orgGroupId = 1;
    var orgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(orgGroupId, "OG", "OG");

    when(userTypeService.getPriorityUserTypeOrThrow(user)).thenReturn(UserType.INDUSTRY);
    when(pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(user, EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles())))
        .thenReturn(List.of(orgGroup));

    var context = consentSearchContextCreator.createContext(user);

    assertThat(context.getUser()).isEqualTo(user);
    assertThat(context.getUserType()).isEqualTo(UserType.INDUSTRY);
    assertThat(context.getOrgGroupIdsUserInTeamFor()).containsExactly(orgGroupId);

  }

  @Test
  void createContext_regulatorUser() {

    when(userTypeService.getPriorityUserTypeOrThrow(user)).thenReturn(UserType.OGA);

    var context = consentSearchContextCreator.createContext(user);
    verifyNoInteractions(pwaHolderTeamService);

    assertThat(context.getUser()).isEqualTo(user);
    assertThat(context.getUserType()).isEqualTo(UserType.OGA);
    assertThat(context.getOrgGroupIdsUserInTeamFor()).isEmpty();

  }

}