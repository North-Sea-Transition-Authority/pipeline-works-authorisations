package uk.co.ogauthority.pwa.service.search.applicationsearch;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSearchContextCreatorTest {

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private TeamService teamService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  private ApplicationSearchContextCreator applicationSearchContextCreator;

  private AuthenticatedUserAccount authenticatedUserAccount;
  private Person person;

  @Before
  public void setup() {
    person = PersonTestUtil.createDefaultPerson();
    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1, person), Collections.emptyList());

    applicationSearchContextCreator = new ApplicationSearchContextCreator(
        userTypeService,
        teamService,
        portalOrganisationsAccessor
    );
  }

  @Test
  public void createContext_contextContentSetAsExpected() {

    var userType = UserType.INDUSTRY;
    when(userTypeService.getUserType(authenticatedUserAccount)).thenReturn(userType);

    var orgUnit = PortalOrganisationTestUtils.getOrganisationUnit();
    var orgGrp = orgUnit.getPortalOrganisationGroup();
    var orgTeam = TeamTestingUtils.getOrganisationTeam(orgGrp);

    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of(orgTeam));
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(Set.of(orgGrp)))
        .thenReturn(List.of(orgUnit));

    var context = applicationSearchContextCreator.createContext(authenticatedUserAccount);

    assertThat(context.getUserType()).isEqualTo(userType);
    assertThat(context.getOrgUnitIdsAssociatedWithHolderTeamMembership()).containsExactly(OrganisationUnitId.from(orgUnit));
    assertThat(context.getOrgGroupsWhereMemberOfHolderTeam()).containsExactly(orgGrp);

  }
}