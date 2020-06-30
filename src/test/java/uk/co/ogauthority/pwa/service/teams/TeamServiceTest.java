package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamMemberDto;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.service.teams.PortalTeamAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class TeamServiceTest {

  @Mock
  private PortalTeamAccessor portalTeamAccessor;

  @Mock
  private PwaTeamsDtoFactory pwaTeamsDtoFactory;

  @Mock
  private PwaUserPrivilegeService pwaUserPrivilegeService;

  @Captor
  private ArgumentCaptor<List<String>> stringListCaptor;

  private TeamService teamService;
  private Person regulatorPerson;
  private Person organisationPerson;
  private PwaRegulatorTeam regulatorTeam;
  private PortalTeamDto regulatorTeamAsPortalTeamDto;
  private PortalTeamMemberDto regulatorTeamMemberDto;
  private List<PortalTeamMemberDto> regulatorTeamMembers;
  private PwaOrganisationTeam organisationTeam1;
  private PwaOrganisationTeam organisationTeam2;
  private PortalTeamDto organisationTeamAsPortalTeamDto1;
  private PortalTeamDto organisationTeamAsPortalTeamDto2;
  private WebUserAccount someWebUserAccount = new WebUserAccount(99);

  @Before
  public void setup() {
    regulatorPerson = new Person(1, "reg", "person", "reg@person.com", "0");
    organisationPerson = new Person(2, "org", "person", "org@person.com", "0");

    teamService = new TeamService(portalTeamAccessor, pwaTeamsDtoFactory, pwaUserPrivilegeService);

    regulatorTeam = TeamTestingUtils.getRegulatorTeam();
    regulatorTeamAsPortalTeamDto = TeamTestingUtils.portalTeamDtoFrom(regulatorTeam);

    when(portalTeamAccessor.getPortalTeamsByPortalTeamType(PwaTeamType.REGULATOR.getPortalTeamType()))
        .thenReturn(List.of(regulatorTeamAsPortalTeamDto));

    when(portalTeamAccessor.findPortalTeamById(regulatorTeam.getId()))
        .thenReturn(Optional.of(regulatorTeamAsPortalTeamDto));

    when(pwaTeamsDtoFactory.createRegulatorTeam(regulatorTeamAsPortalTeamDto)).thenReturn(regulatorTeam);

    regulatorTeamMemberDto = TeamTestingUtils.createPortalTeamMember(regulatorPerson, regulatorTeam);

    regulatorTeamMembers = List.of(regulatorTeamMemberDto);
    when(portalTeamAccessor.getPortalTeamMembers(regulatorTeam.getId()))
        .thenReturn(regulatorTeamMembers);

    var organisationGroup1 = TeamTestingUtils.generateOrganisationGroup(10, "Group1", "Group1 Desc");
    var organisationGroup2 = TeamTestingUtils.generateOrganisationGroup(20, "Group2", "Group2 Desc");
    organisationTeam1 = new PwaOrganisationTeam(11, "org1", "org1", organisationGroup1);
    organisationTeam2 = new PwaOrganisationTeam(22, "org2", "org2", organisationGroup2);
    organisationTeamAsPortalTeamDto1 = TeamTestingUtils.portalTeamDtoFrom(organisationTeam1);
    organisationTeamAsPortalTeamDto2 = TeamTestingUtils.portalTeamDtoFrom(organisationTeam2);

    when(portalTeamAccessor.getPortalTeamsByPortalTeamType(PwaTeamType.ORGANISATION.getPortalTeamType()))
        .thenReturn(List.of(organisationTeamAsPortalTeamDto1));
  }

  @Test(expected = RuntimeException.class)
  public void getRegulatorTeam_errorWhenMultipleTeamsFound() {
    when(portalTeamAccessor.getPortalTeamsByPortalTeamType(PwaTeamType.REGULATOR.getPortalTeamType()))
        .thenReturn(List.of(regulatorTeamAsPortalTeamDto, regulatorTeamAsPortalTeamDto));

    teamService.getRegulatorTeam();
  }

  @Test(expected = RuntimeException.class)
  public void getRegulatorTeam_errorWhenZeroTeamsFound() {
    when(portalTeamAccessor.getPortalTeamsByPortalTeamType(PwaTeamType.REGULATOR.getPortalTeamType()))
        .thenReturn(List.of());

    teamService.getRegulatorTeam();
  }

  @Test
  public void getRegulatorTeam_callsExpectedFactoryMethod() {
    teamService.getRegulatorTeam();
    verify(pwaTeamsDtoFactory, times(1)).createRegulatorTeam(regulatorTeamAsPortalTeamDto);
  }

  @Test
  public void getAllOrganisationTeams_callsExpectedFactoryMethod() {
    teamService.getAllOrganisationTeams();
    verify(pwaTeamsDtoFactory, times(1)).createOrganisationTeamList(List.of(organisationTeamAsPortalTeamDto1));
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getTeamByResId_errorThrownWhenTeamNotFound() {
    teamService.getTeamByResId(999);
  }

  @Test
  public void getTeamByResId_callsExpectedFactoryMethodWhenTeamFound() {
    when(pwaTeamsDtoFactory.createPwaTeam(regulatorTeamAsPortalTeamDto)).thenReturn(regulatorTeam);

    teamService.getTeamByResId(regulatorTeam.getId());

    verify(pwaTeamsDtoFactory, times(1)).createPwaTeam(regulatorTeamAsPortalTeamDto);
  }

  @Test
  public void getTeamMembers_callsServiceMethodsWithExpectedValues() {
    teamService.getTeamMembers(regulatorTeam);

    verify(portalTeamAccessor, times(1)).getPortalTeamMembers(regulatorTeam.getId());
    verify(pwaTeamsDtoFactory, times(1)).createPwaTeamMemberList(eq(regulatorTeamMembers), eq(regulatorTeam));
  }

  @Test
  public void getMembershipOfPersonInTeam_emptyOptionalWhenNotATeamMember() {
    assertThat(teamService.getMembershipOfPersonInTeam(regulatorTeam, organisationPerson).isPresent()).isFalse();
  }

  @Test
  public void getMembershipOfPersonInTeam_populatedOptionalWhenTeamMember() {
    when(portalTeamAccessor.getPersonTeamMembership(eq(regulatorPerson), eq(regulatorTeam.getId())))
        .thenReturn(Optional.of(regulatorTeamMemberDto));
    when(pwaTeamsDtoFactory.createPwaTeamMember(eq(regulatorTeamMemberDto), eq(regulatorPerson), eq(regulatorTeam)))
        .thenReturn(mock(PwaTeamMember.class));

    assertThat(teamService.getMembershipOfPersonInTeam(regulatorTeam, regulatorPerson)).isNotEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void getRegulatorTeamIfPersonInRole_whenNoRolesProvided() {
    teamService.getRegulatorTeamIfPersonInRole(regulatorPerson, Set.of());
  }

  @Test
  public void getRegulatorTeamIfPersonInRole_whenNotInProvidedRoles() {
    when(portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
      eq(regulatorPerson),
      eq(PwaTeamType.REGULATOR.getPortalTeamType()),
      eq(List.of(PwaRegulatorRole.ORGANISATION_MANAGER.getPortalTeamRoleName()))))
        .thenReturn(List.of());

    assertThat(teamService.getRegulatorTeamIfPersonInRole(regulatorPerson, List.of(PwaRegulatorRole.ORGANISATION_MANAGER)))
        .isEmpty();
  }

  @Test
  public void getRegulatorTeamIfPersonInRole_whenPersonInRoles() {
    when(portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
      eq(regulatorPerson),
      eq(PwaTeamType.REGULATOR.getPortalTeamType()),
      any()))
        .thenReturn(List.of(regulatorTeamAsPortalTeamDto));

    assertThat(teamService.getRegulatorTeamIfPersonInRole(regulatorPerson, EnumSet.allOf(PwaRegulatorRole.class)))
        .isNotEmpty();

    verify(portalTeamAccessor).getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
        eq(regulatorPerson),
        any(),
        stringListCaptor.capture()
    );

    List<String> expectedRolesArgumentToPortalTeamsAPI = EnumSet.allOf(PwaRegulatorRole.class).stream()
        .map(PwaRegulatorRole::getPortalTeamRoleName)
        .collect(Collectors.toList());

    assertThat(stringListCaptor.getValue()).containsExactlyInAnyOrderElementsOf(expectedRolesArgumentToPortalTeamsAPI);

    verify(pwaTeamsDtoFactory, times(1)).createRegulatorTeam(regulatorTeamAsPortalTeamDto);
  }

  @Test
  public void getOrganisationTeamListIfPersonInRole_whenPersonInRoles_andSingleTeamMatched() {
    List<PortalTeamDto> foundOrganisationTeams = List.of(organisationTeamAsPortalTeamDto1);

    when(portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
      eq(organisationPerson),
      eq(PwaTeamType.ORGANISATION.getPortalTeamType()),
      any()))
        .thenReturn(foundOrganisationTeams);

    when(pwaTeamsDtoFactory.createOrganisationTeamList(foundOrganisationTeams))
        .thenReturn(List.of(organisationTeam1));

    assertThat(teamService.getOrganisationTeamListIfPersonInRole(organisationPerson, EnumSet.allOf(PwaOrganisationRole.class)))
        .containsExactly(organisationTeam1);

    verify(portalTeamAccessor).getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
        eq(organisationPerson),
        any(),
        stringListCaptor.capture()
    );

    List<String> expectedRoles = EnumSet.allOf(PwaOrganisationRole.class).stream()
        .map(PwaOrganisationRole::getPortalTeamRoleName)
        .collect(Collectors.toList());

    assertThat(stringListCaptor.getValue()).containsExactlyInAnyOrderElementsOf(expectedRoles);

    verify(pwaTeamsDtoFactory, times(1)).createOrganisationTeamList(foundOrganisationTeams);
  }

  @Test
  public void getOrganisationTeamListIfPersonInRole_whenPersonInRoles_andMultipleTeamsMatched() {
    List<PortalTeamDto> foundOrganisationTeams = List.of(organisationTeamAsPortalTeamDto1, organisationTeamAsPortalTeamDto2);

    when(portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
      eq(organisationPerson),
      eq(PwaTeamType.ORGANISATION.getPortalTeamType()),
      any()))
        .thenReturn(foundOrganisationTeams);

    when(pwaTeamsDtoFactory.createOrganisationTeamList(foundOrganisationTeams))
        .thenReturn(List.of(organisationTeam1, organisationTeam2));

    assertThat(teamService.getOrganisationTeamListIfPersonInRole(organisationPerson, EnumSet.allOf(PwaOrganisationRole.class)))
        .containsExactly(organisationTeam1, organisationTeam2);

    verify(pwaTeamsDtoFactory, times(1)).createOrganisationTeamList(foundOrganisationTeams);
  }

  @Test
  public void getOrganisationTeamListIfPersonInRole_whenPersonInRoles_anNoTeamsMatched() {
    assertThat(teamService.getOrganisationTeamListIfPersonInRole(regulatorPerson, EnumSet.allOf(PwaOrganisationRole.class)))
        .isEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void getOrganisationTeamListIfPersonInRole_errorWhenNoRolesProvided() {
    teamService.getOrganisationTeamListIfPersonInRole(regulatorPerson, Set.of());
  }

  @Test
  public void addPersonToTeamInRoles_verifyServiceInteraction() {
    var roles = List.of("some_role_1", "some_role_2");
    teamService.addPersonToTeamInRoles(regulatorTeam, organisationPerson, roles, someWebUserAccount);

    verify(portalTeamAccessor, times(1))
        .addPersonToTeamWithRoles(regulatorTeam.getId(), organisationPerson, roles, someWebUserAccount);
  }

  @Test
  public void removePersonFromTeam_verifyServiceInteraction() {
    teamService.removePersonFromTeam(regulatorTeam, regulatorPerson, someWebUserAccount);
    verify(portalTeamAccessor, times(1)).removePersonFromTeam(regulatorTeam.getId(), regulatorPerson, someWebUserAccount);
  }

  @Test
  public void personIsMemberOfTeam_verifyServiceInteractions() {
    teamService.isPersonMemberOfTeam(regulatorPerson, regulatorTeam);
    verify(portalTeamAccessor, times(1)).personIsAMemberOfTeam(regulatorTeam.getId(), regulatorPerson);
  }

  @Test
  public void getAllRolesForTeam_whenMultipleRolesReturned() {
    var mockRole = mock(PwaRole.class);
    when(pwaTeamsDtoFactory.createPwaRole(any())).thenReturn(mockRole);
    when(portalTeamAccessor.getAllPortalRolesForTeam(eq(regulatorTeam.getId())))
        .thenReturn(List.of(TeamTestingUtils.getTeamAdminRoleDto(regulatorTeam), TeamTestingUtils.getTeamAdminRoleDto(regulatorTeam)));

    assertThat(teamService.getAllRolesForTeam(regulatorTeam)).containsExactly(mockRole, mockRole);
  }

  @Test
  public void getOrganisationTeamsPersonIsMemberOf_verifyServiceInteractions() {
    teamService.getOrganisationTeamsPersonIsMemberOf(organisationPerson);

    verify(portalTeamAccessor, times(1)).getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
        eq(organisationPerson),
        eq(PwaTeamType.ORGANISATION.getPortalTeamType()),
        stringListCaptor.capture()
    );

    List<String> expectedRoles = EnumSet.allOf(PwaOrganisationRole.class).stream()
        .map(PwaOrganisationRole::getPortalTeamRoleName)
        .collect(Collectors.toList());

    assertThat(stringListCaptor.getValue()).containsExactlyInAnyOrderElementsOf(expectedRoles);
  }

  @Test
  public void getAllUserPrivilegesForPerson_portalPrivs_noAppPrivs() {

    when(pwaTeamsDtoFactory.createPwaUserPrivilegeSet(any())).thenReturn(Set.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_REG_ORG_MANAGE));

    when(pwaUserPrivilegeService.getPwaUserPrivilegesForPerson(organisationPerson)).thenReturn(Set.of());

    var privSet = teamService.getAllUserPrivilegesForPerson(organisationPerson);

    assertThat(privSet).containsExactlyInAnyOrder(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_REG_ORG_MANAGE);

  }

  @Test
  public void getAllUserPrivilegesForPerson_portalPrivs_appPrivs_noOverlap() {

    when(pwaTeamsDtoFactory.createPwaUserPrivilegeSet(any())).thenReturn(Set.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE));

    when(pwaUserPrivilegeService.getPwaUserPrivilegesForPerson(organisationPerson)).thenReturn(Set.of(PwaUserPrivilege.PWA_WORKAREA));

    var privSet = teamService.getAllUserPrivilegesForPerson(organisationPerson);

    assertThat(privSet).containsExactlyInAnyOrder(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_REG_ORG_MANAGE);

  }

  @Test
  public void getAllUserPrivilegesForPerson_portalPrivs_appPrivs_overlap() {

    when(pwaTeamsDtoFactory.createPwaUserPrivilegeSet(any())).thenReturn(Set.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE, PwaUserPrivilege.PWA_WORKAREA));

    when(pwaUserPrivilegeService.getPwaUserPrivilegesForPerson(organisationPerson)).thenReturn(Set.of(PwaUserPrivilege.PWA_WORKAREA));

    var privSet = teamService.getAllUserPrivilegesForPerson(organisationPerson);

    assertThat(privSet).containsExactlyInAnyOrder(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_REG_ORG_MANAGE);

  }

  @Test
  public void getAllUserPrivilegesForPerson_NoPortalPrivs_appPrivs() {

    when(pwaTeamsDtoFactory.createPwaUserPrivilegeSet(any())).thenReturn(Set.of());

    when(pwaUserPrivilegeService.getPwaUserPrivilegesForPerson(organisationPerson)).thenReturn(Set.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_CONSULTEE_GROUP_ADMIN));

    var privSet = teamService.getAllUserPrivilegesForPerson(organisationPerson);

    assertThat(privSet).containsExactlyInAnyOrder(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_CONSULTEE_GROUP_ADMIN);

  }

}
