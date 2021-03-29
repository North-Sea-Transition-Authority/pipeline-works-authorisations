package uk.co.ogauthority.pwa.service.orgs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaOrganisationAccessorTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private TeamService teamService;

  @Mock
  private UserTypeService userTypeService;

  private PwaOrganisationAccessor pwaOrganisationAccessor;

  private AuthenticatedUserAccount industryUser, ogaUser;

  private PortalOrganisationGroup organisationGroup1;

  @Before
  public void setUp() {

    industryUser = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createPersonFrom(new PersonId(1))), Set.of());
    ogaUser = new AuthenticatedUserAccount(new WebUserAccount(2, PersonTestUtil.createPersonFrom(new PersonId(2))), Set.of());

    when(userTypeService.getPriorityUserType(industryUser)).thenReturn(UserType.INDUSTRY);
    when(userTypeService.getPriorityUserType(ogaUser)).thenReturn(UserType.OGA);

    pwaOrganisationAccessor = new PwaOrganisationAccessor(teamService, portalOrganisationsAccessor, userTypeService);

    organisationGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(1,"ONE", "O");
    organisationGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(2,"TWO", "T");

  }

  @Test
  public void industryUser_getOrgGrps_restricted() {

    var team = TeamTestingUtils.getOrganisationTeam(organisationGroup1);

    when(teamService.getOrganisationTeamListIfPersonInRole(industryUser.getLinkedPerson(), List.of(PwaOrganisationRole.APPLICATION_CREATOR)))
        .thenReturn(List.of(team));

    var groups = pwaOrganisationAccessor.getOrgGroupsUserCanAccess(industryUser);

    assertThat(groups).containsExactly(organisationGroup1);

  }

  @Test
  public void industryUser_getOrgUnits_restricted() {

    var team = TeamTestingUtils.getOrganisationTeam(organisationGroup1);

    when(teamService.getOrganisationTeamListIfPersonInRole(industryUser.getLinkedPerson(), List.of(PwaOrganisationRole.APPLICATION_CREATOR)))
        .thenReturn(List.of(team));

    pwaOrganisationAccessor.getOrgUnitsUserCanAccess(industryUser);

    verify(portalOrganisationsAccessor, times(1)).getOrganisationUnitsForOrganisationGroupsIn(eq(List.of(organisationGroup1)));

  }

  @Test
  public void ogaUser_getOrgGrps_all() {

    pwaOrganisationAccessor.getOrgGroupsUserCanAccess(ogaUser);

    verify(portalOrganisationsAccessor, times(1)).getAllOrganisationGroups();

  }

  @Test
  public void ogaUser_getOrgUnits_all() {

    pwaOrganisationAccessor.getOrgUnitsUserCanAccess(ogaUser);

    verify(portalOrganisationsAccessor, times(1)).getAllOrganisationUnits();

  }

}
