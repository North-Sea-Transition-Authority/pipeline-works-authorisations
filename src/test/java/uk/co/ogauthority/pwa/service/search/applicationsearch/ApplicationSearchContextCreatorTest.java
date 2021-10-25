package uk.co.ogauthority.pwa.service.search.applicationsearch;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSearchContextCreatorTest {

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private TeamService teamService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  private ApplicationSearchContextCreator applicationSearchContextCreator;

  private ConsulteeGroup consulteeGroup;

  private AuthenticatedUserAccount authenticatedUserAccount;
  private Person person;

  @Before
  public void setup() {
    person = PersonTestUtil.createDefaultPerson();
    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1, person), Collections.emptyList());

    applicationSearchContextCreator = new ApplicationSearchContextCreator(
        userTypeService,
        teamService,
        portalOrganisationsAccessor,
        consulteeGroupTeamService);
  }

  @Test
  public void createContext_contextContentSetAsExpected() {

    var userType = UserType.INDUSTRY;
    when(userTypeService.getUserTypes(authenticatedUserAccount)).thenReturn(Set.of(userType));

    var orgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var orgGrp = orgUnit.getPortalOrganisationGroup().get();
    var orgTeam = TeamTestingUtils.getOrganisationTeam(orgGrp);
    consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    var consultationGroupTeamMember = new ConsulteeGroupTeamMember(
        consulteeGroup,
        person,
        EnumSet.allOf(ConsulteeGroupMemberRole.class)
    );

    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of(orgTeam));
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(Set.of(orgGrp)))
        .thenReturn(List.of(orgUnit));
    when(consulteeGroupTeamService.getTeamMemberByPerson(person)).thenReturn(Optional.of(consultationGroupTeamMember));

    var context = applicationSearchContextCreator.createContext(authenticatedUserAccount);

    assertThat(context.getUserTypes()).containsExactly(userType);
    assertThat(context.getOrgUnitIdsAssociatedWithHolderTeamMembership()).containsExactly(OrganisationUnitId.from(orgUnit));
    assertThat(context.getOrgGroupsWhereMemberOfHolderTeam()).containsExactly(orgGrp);
    assertThat(context.getConsulteeGroupIds()).containsExactly(ConsulteeGroupId.from(consulteeGroup));

  }
}