package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.internal.PersonRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalRoleDto;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalSystemPrivilegeDto;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamDto;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamMemberDto;
import uk.co.ogauthority.pwa.model.teams.PwaGlobalTeam;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PwaTeamsDtoFactoryTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PersonRepository personRepository;

  private PwaTeamsDtoFactory pwaTeamsDtoFactory;

  private PortalTeamDto regulatorTeamDto;
  private PortalTeamDto organisationTeamDto1;
  private PortalTeamDto organisationTeamDto2;
  private PortalTeamDto globalTeamDto;

  private PortalOrganisationGroup portalOrganisationGroup1;
  private PortalOrganisationGroup portalOrganisationGroup2;
  private PwaRegulatorTeam regulatorTeam;
  private PwaOrganisationTeam organisationTeam1;
  private Person orgMember1;
  private Person orgMember2;
  private PortalTeamMemberDto orgTeamMember1;
  private PortalRoleDto role1;
  private PortalRoleDto role2;

  @BeforeEach
  void setup() {
    pwaTeamsDtoFactory = new PwaTeamsDtoFactory(portalOrganisationsAccessor, personRepository);

    setupPortalTeamDtos();
    organisationTeam1 = TeamTestingUtils.getOrganisationTeam(portalOrganisationGroup1);

    orgMember1 = new Person(1, "Person", "One", "person@onw.com", "0");
    orgMember2 = new Person(2, "Person", "Two", "person@two.com", "0");

    setupOrgTeamMember();

    when(portalOrganisationsAccessor.getAllOrganisationGroupsWithUrefIn(any()))
        .thenReturn(List.of(portalOrganisationGroup1));
  }

  private void setupPortalTeamDtos() {
    portalOrganisationGroup1 = TeamTestingUtils.generateOrganisationGroup(100, "GROUP1", "GRP1");
    portalOrganisationGroup2 = TeamTestingUtils.generateOrganisationGroup(200, "GROUP2", "GRP2");

    regulatorTeamDto = new PortalTeamDto(10, "NAME", "DESC", PwaTeamType.REGULATOR.getPortalTeamType(), null);
    organisationTeamDto1 = new PortalTeamDto(20, "NAME1", "DESC1", PwaTeamType.ORGANISATION.getPortalTeamType(), portalOrganisationGroup1.getUrefValue());
    organisationTeamDto2 = new PortalTeamDto(30, "NAME2", "DESC2", PwaTeamType.ORGANISATION.getPortalTeamType(), portalOrganisationGroup2.getUrefValue());
    globalTeamDto = new PortalTeamDto(40, "GL", "GL", PwaTeamType.GLOBAL.getPortalTeamType(), null);
  }

  private void setupOrgTeamMember() {
    role1 = new PortalRoleDto(organisationTeamDto1.getResId(), "role1", "title1", "desc1", 10);
    role2 = new PortalRoleDto(organisationTeamDto1.getResId(), "role2", "title2", "desc2", 20);
    orgTeamMember1 = new PortalTeamMemberDto(orgMember1.getId(), Set.of(role1, role2));
  }

  @Test
  void createPwaTeam_errorsWhenTeamTypeNotSupported() {
    var unsupportedTeamType = new PortalTeamDto(1, "NAME", "DESC", "UNSUPPORTED", null);
    assertThrows(IllegalArgumentException.class, () ->
      pwaTeamsDtoFactory.createPwaTeam(unsupportedTeamType));
  }

  @Test
  void createPwaTeam_createsRegulatorInstanceWhenExpected() {
    assertThat(pwaTeamsDtoFactory.createPwaTeam(regulatorTeamDto)).isInstanceOf(PwaRegulatorTeam.class);
  }

  @Test
  void createPwaTeam_createsOrganisationInstanceWhenExpected() {
    assertThat(pwaTeamsDtoFactory.createPwaTeam(organisationTeamDto1)).isInstanceOf(PwaOrganisationTeam.class);
  }

  @Test
  void createPwaTeam_createsGlobalInstanceWhenExpected() {
    assertThat(pwaTeamsDtoFactory.createPwaTeam(globalTeamDto)).isInstanceOf(PwaGlobalTeam.class);
  }

  @Test
  void createRegulatorTeam_errorsWhenGivenUnexpectedTeamTypeDto() {
    assertThrows(PwaTeamFactoryException.class, () ->
      pwaTeamsDtoFactory.createRegulatorTeam(organisationTeamDto1));
  }

  @Test
  void createRegulatorTeam_mapsDtoAsExpected() {
    regulatorTeam = pwaTeamsDtoFactory.createRegulatorTeam(regulatorTeamDto);
    assertThat(regulatorTeam.getId()).isEqualTo(regulatorTeamDto.getResId());
    assertThat(regulatorTeam.getName()).isEqualTo(regulatorTeamDto.getName());
    assertThat(regulatorTeam.getDescription()).isEqualTo(regulatorTeamDto.getDescription());
    assertThat(regulatorTeam.getType()).isEqualTo(PwaTeamType.REGULATOR);
  }

  @Test
  void createOrganisationTeam_mapsDtoAsExpected() {
    List<String> expectedUrefList = List.of(portalOrganisationGroup1.getUrefValue());
    organisationTeam1 = pwaTeamsDtoFactory.createOrganisationTeam(organisationTeamDto1);
    verify(portalOrganisationsAccessor, times(1)).getAllOrganisationGroupsWithUrefIn(eq(expectedUrefList));
    verifyNoMoreInteractions(portalOrganisationsAccessor);

    assertThat(organisationTeam1.getId()).isEqualTo(organisationTeamDto1.getResId());
    assertThat(organisationTeam1.getName()).isEqualTo(portalOrganisationGroup1.getName());
    assertThat(organisationTeam1.getDescription()).isEqualTo(organisationTeamDto1.getName());
    assertThat(organisationTeam1.getPortalOrganisationGroup()).isEqualTo(portalOrganisationGroup1);
  }

  @Test
  void createOrganisationTeam_errorsWhenAssociatedUrefNotValidOrganisationGroup() {
    when(portalOrganisationsAccessor.getAllOrganisationGroupsWithUrefIn(any())).thenReturn(List.of());
    assertThrows(PwaTeamFactoryException.class, () ->
      pwaTeamsDtoFactory.createOrganisationTeam(organisationTeamDto1));
  }

  @Test
  void createOrganisationTeam_errorsWhenAssociatedUrefNotScoped() {
    when(portalOrganisationsAccessor.getAllOrganisationGroupsWithUrefIn(any())).thenReturn(List.of());
    organisationTeamDto1 = new PortalTeamDto(1, "NAME", "DESC", PwaTeamType.ORGANISATION.getPortalTeamType(), null);
    assertThrows(PwaTeamFactoryException.class, () ->

      pwaTeamsDtoFactory.createOrganisationTeam(organisationTeamDto1));
  }

  @Test
  void createOrganisationTeamList_verifyOrganisationsRetrievedInOneHit() {
    List<String> expectedUrefList = List.of(portalOrganisationGroup1.getUrefValue(), portalOrganisationGroup2.getUrefValue());
    when(portalOrganisationsAccessor.getAllOrganisationGroupsWithUrefIn(any()))
        .thenReturn(List.of(portalOrganisationGroup1, portalOrganisationGroup2));

    pwaTeamsDtoFactory.createOrganisationTeamList(List.of(organisationTeamDto1, organisationTeamDto2));
    verify(portalOrganisationsAccessor, times(1)).getAllOrganisationGroupsWithUrefIn(eq(expectedUrefList));
    verifyNoMoreInteractions(portalOrganisationsAccessor);
  }

  @Test
  void createPwaTeamMemberList_verifyTeamMemberPersonsGotInOneHit() {
    PortalTeamMemberDto orgTeamMember1 = TeamTestingUtils.createPortalTeamMember(orgMember1, organisationTeam1);
    PortalTeamMemberDto orgTeamMember2 = TeamTestingUtils.createPortalTeamMember(orgMember2, organisationTeam1);
    List<PortalTeamMemberDto> portalTeamMemberDtos = List.of(orgTeamMember1, orgTeamMember2);

    List<PwaTeamMember> pwaTeamMembers = pwaTeamsDtoFactory.createPwaTeamMemberList(portalTeamMemberDtos, organisationTeam1);

    assertThat(pwaTeamMembers).hasSize(2);
    verify(personRepository, times(1))
        .findAllByIdIn(eq(Set.of(orgMember1.getId().asInt(), orgMember2.getId().asInt())));
  }

  @Test
  void createPwaTeamMember_mapsDtoPropertiesAsExpected() {
    PwaTeamMember teamMember = pwaTeamsDtoFactory.createPwaTeamMember(orgTeamMember1, orgMember1, organisationTeam1);

    assertThat(teamMember.getPerson()).isEqualTo(orgMember1);
    assertThat(teamMember.getTeam()).isEqualTo(organisationTeam1);
  }

  @Test
  void createPwaTeamMember_mapsRolesAsExpected() {
    PwaTeamMember teamMember = pwaTeamsDtoFactory.createPwaTeamMember(orgTeamMember1, orgMember1, organisationTeam1);

    PwaRole mappedRole1 = teamMember.getRoleSet().stream()
        .filter(r -> r.getName().equals(role1.getName()))
        .findFirst()
        .get();

    PwaRole mappedRole2 = teamMember.getRoleSet().stream()
        .filter(r -> r.getName().equals(role2.getName()))
        .findFirst()
        .get();

    assertRoleMappedAsExpected(mappedRole1, role1);
    assertRoleMappedAsExpected(mappedRole2, role2);
  }

  private void assertRoleMappedAsExpected(PwaRole mappedRole, PortalRoleDto roleDtoToMap) {
    assertThat(mappedRole.getName()).isEqualTo(roleDtoToMap.getName());
    assertThat(mappedRole.getDescription()).isEqualTo(roleDtoToMap.getDescription());
    assertThat(mappedRole.getTitle()).isEqualTo(roleDtoToMap.getTitle());
    assertThat(mappedRole.getDisplaySequence()).isEqualTo(roleDtoToMap.getDisplaySequence());
  }


  @Test
  void createPwaUserPrivilegeSet_mapsPrivAsExpected_andRemovesDuplicates() {
    Set<PwaUserPrivilege> privs = pwaTeamsDtoFactory.createPwaUserPrivilegeSet(List.of(
        new PortalSystemPrivilegeDto(PwaTeamType.REGULATOR.getPortalTeamType(), "SomeRole",
            PwaUserPrivilege.PWA_WORKAREA.name()),
        new PortalSystemPrivilegeDto(PwaTeamType.ORGANISATION.getPortalTeamType(), "SomeOtherRole",
            PwaUserPrivilege.PWA_WORKAREA.name()),
        new PortalSystemPrivilegeDto(PwaTeamType.ORGANISATION.getPortalTeamType(), "DifferentRole",
            "unknown priv")
    ));

    assertThat(privs).hasSize(1);
    assertThat(privs).containsExactly(PwaUserPrivilege.PWA_WORKAREA);
  }

}
