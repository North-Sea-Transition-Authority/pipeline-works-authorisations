package uk.co.ogauthority.pwa.service.orgs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PwaOrganisationAccessorTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private UserTypeService userTypeService;

  @InjectMocks
  private PwaOrganisationAccessor pwaOrganisationAccessor;

  private AuthenticatedUserAccount industryUser, ogaUser;

  private PortalOrganisationGroup organisationGroup1;

  @BeforeEach
  void setUp() {

    industryUser = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createPersonFrom(new PersonId(1))), Set.of());
    ogaUser = new AuthenticatedUserAccount(new WebUserAccount(2, PersonTestUtil.createPersonFrom(new PersonId(2))), Set.of());

    when(userTypeService.getPriorityUserTypeOrThrow(industryUser)).thenReturn(UserType.INDUSTRY);
    when(userTypeService.getPriorityUserTypeOrThrow(ogaUser)).thenReturn(UserType.OGA);

    organisationGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(1,"ONE", "O");
    organisationGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(2,"TWO", "T");

  }

  @Test
  void industryUser_getOrgGrps_restricted() {

    var team = new Team();
    var teamId = 1;
    team.setScopeId(String.valueOf(teamId));

    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(industryUser.getWuaId(), TeamType.ORGANISATION, List.of(Role.APPLICATION_CREATOR)))
        .thenReturn(List.of(team));
    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of(teamId))).thenReturn(List.of(organisationGroup1));

    var result = pwaOrganisationAccessor.getOrgGroupsUserCanAccess(industryUser);

    assertThat(result).containsExactly(organisationGroup1);

  }


  @Test
  void getOrganisationGroupOrError_orgGroupNotFound() {
    assertThrows(PwaEntityNotFoundException.class, () ->
      pwaOrganisationAccessor.getOrganisationGroupOrError(1));
  }

  @Test
  void getOrganisationGroupOrError_orgGroupFound() {
    var portalOrgGroup = new PortalOrganisationGroup();
    when(portalOrganisationsAccessor.getOrganisationGroupById(1)).thenReturn(Optional.of(portalOrgGroup));
    assertThat(pwaOrganisationAccessor.getOrganisationGroupOrError(1)).isEqualTo(portalOrgGroup);
  }


  @Test
  void industryUser_getOrgUnits_restricted() {

    var team = new Team();
    var teamId = 1;
    team.setScopeId(String.valueOf(teamId));

    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(industryUser.getWuaId(), TeamType.ORGANISATION, List.of(Role.APPLICATION_CREATOR)))
        .thenReturn(List.of(team));
    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of(teamId))).thenReturn(List.of(organisationGroup1));

    pwaOrganisationAccessor.getOrgUnitsUserCanAccess(industryUser);

    verify(portalOrganisationsAccessor).getSearchableOrganisationUnitsForOrganisationGroupsIn(List.of(organisationGroup1));

  }

  @Test
  void ogaUser_getOrgGrps_all() {

    pwaOrganisationAccessor.getOrgGroupsUserCanAccess(ogaUser);

    verify(portalOrganisationsAccessor).getAllOrganisationGroups();

  }

  @Test
  void ogaUser_getOrgUnits_all() {

    pwaOrganisationAccessor.getOrgUnitsUserCanAccess(ogaUser);

    verify(portalOrganisationsAccessor).getAllActiveOrganisationUnitsSearch();

  }

}
