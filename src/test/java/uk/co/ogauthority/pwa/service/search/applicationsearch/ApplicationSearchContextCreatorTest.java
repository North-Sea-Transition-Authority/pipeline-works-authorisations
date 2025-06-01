package uk.co.ogauthority.pwa.service.search.applicationsearch;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ApplicationSearchContextCreatorTest {

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private ApplicationSearchContextCreator applicationSearchContextCreator;

  private AuthenticatedUserAccount authenticatedUserAccount;
  private Person person;

  @BeforeEach
  void setup() {
    person = PersonTestUtil.createDefaultPerson();
    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1, person), Collections.emptyList());
  }

  @Test
  void createContext_contextContentSetAsExpected() {

    var userType = UserType.INDUSTRY;
    when(userTypeService.getUserTypes(authenticatedUserAccount)).thenReturn(Set.of(userType));

    var orgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var orgGrp = orgUnit.getPortalOrganisationGroup().get();
    ConsulteeGroup consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);

    when(pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(authenticatedUserAccount, EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles())))
        .thenReturn(List.of(orgGrp));
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(List.of(orgGrp)))
        .thenReturn(List.of(orgUnit));
    var team = new Team(UUID.randomUUID());
    team.setScopeId(String.valueOf(consulteeGroup.getId()));
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(eq(((long) authenticatedUserAccount.getWuaId())), eq(TeamType.CONSULTEE), anySet()))
        .thenReturn(List.of(team));

    var context = applicationSearchContextCreator.createContext(authenticatedUserAccount);

    assertThat(context.getUserTypes()).containsExactly(userType);
    assertThat(context.getOrgUnitIdsAssociatedWithHolderTeamMembership()).containsExactly(OrganisationUnitId.from(orgUnit));
    assertThat(context.getOrgGroupsWhereMemberOfHolderTeam()).containsExactly(orgGrp);
    assertThat(context.getConsulteeGroupIds()).containsExactly(ConsulteeGroupId.from(consulteeGroup));

  }
}