package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalRoleDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalSystemPrivilegeDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamMemberDto;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.repository.PersonRepository;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;
import uk.co.ogauthority.pwa.util.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaTeamsDtoFactoryTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PersonRepository personRepository;

  private PwaTeamsDtoFactory pwaTeamsDtoFactory;

  private PortalTeamDto regulatorTeamDto;
  private PortalTeamDto organisationTeamDto1;
  private PortalTeamDto organisationTeamDto2;
  private PortalOrganisationGroup portalOrganisationGroup1;
  private PortalOrganisationGroup portalOrganisationGroup2;
  private PwaRegulatorTeam regulatorTeam;
  private PwaOrganisationTeam organisationTeam1;
  private Person orgMember1;
  private Person orgMember2;
  private PortalTeamMemberDto orgTeamMember1;
  private PortalRoleDto role1;
  private PortalRoleDto role2;

  @Before
  public void setup() {
    pwaTeamsDtoFactory = new PwaTeamsDtoFactory(portalOrganisationsAccessor, personRepository);

    setupPortalTeamDtos();
    organisationTeam1 = TeamTestingUtils.getOrganisationTeam(portalOrganisationGroup1);

    orgMember1 = new Person(1, "Person", "One", "person@onw.com");
    orgMember2 = new Person(2, "Person", "Two", "person@two.com");

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
  }

  private void setupOrgTeamMember() {
    role1 = new PortalRoleDto(organisationTeamDto1.getResId(), "role1", "title1", "desc1", 10);
    role2 = new PortalRoleDto(organisationTeamDto1.getResId(), "role2", "title2", "desc2", 20);
    orgTeamMember1 = new PortalTeamMemberDto(orgMember1.getId(), Set.of(role1, role2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createPwaTeam_errorsWhenTeamTypeNotSupported() {
    var unsupportedTeamType = new PortalTeamDto(1, "NAME", "DESC", "UNSUPPORTED", null);
    pwaTeamsDtoFactory.createPwaTeam(unsupportedTeamType);
  }

  @Test
  public void createPwaTeam_createsRegulatorInstanceWhenExpected() {
    assertThat(pwaTeamsDtoFactory.createPwaTeam(regulatorTeamDto)).isInstanceOf(PwaRegulatorTeam.class);
  }

  @Test
  public void createPwaTeam_createsOrganisationInstanceWhenExpected() {
    assertThat(pwaTeamsDtoFactory.createPwaTeam(organisationTeamDto1)).isInstanceOf(PwaOrganisationTeam.class);
  }

  @Test(expected = PwaTeamFactoryException.class)
  public void createRegulatorTeam_errorsWhenGivenUnexpectedTeamTypeDto() {
    pwaTeamsDtoFactory.createRegulatorTeam(organisationTeamDto1);
  }

  @Test
  public void createRegulatorTeam_mapsDtoAsExpected() {
    regulatorTeam = pwaTeamsDtoFactory.createRegulatorTeam(regulatorTeamDto);
    assertThat(regulatorTeam.getId()).isEqualTo(regulatorTeamDto.getResId());
    assertThat(regulatorTeam.getName()).isEqualTo(regulatorTeamDto.getName());
    assertThat(regulatorTeam.getDescription()).isEqualTo(regulatorTeamDto.getDescription());
    assertThat(regulatorTeam.getType()).isEqualTo(PwaTeamType.REGULATOR);
  }

  @Test
  public void createOrganisationTeam_mapsDtoAsExpected() {
    List<String> expectedUrefList = List.of(portalOrganisationGroup1.getUrefValue());
    organisationTeam1 = pwaTeamsDtoFactory.createOrganisationTeam(organisationTeamDto1);
    verify(portalOrganisationsAccessor, times(1)).getAllOrganisationGroupsWithUrefIn(eq(expectedUrefList));
    verifyNoMoreInteractions(portalOrganisationsAccessor);

    assertThat(organisationTeam1.getId()).isEqualTo(organisationTeamDto1.getResId());
    assertThat(organisationTeam1.getName()).isEqualTo(portalOrganisationGroup1.getName());
    assertThat(organisationTeam1.getDescription()).isEqualTo(organisationTeamDto1.getName());
    assertThat(organisationTeam1.getPortalOrganisationGroup()).isEqualTo(portalOrganisationGroup1);
  }

  @Test(expected = PwaTeamFactoryException.class)
  public void createOrganisationTeam_errorsWhenAssociatedUrefNotValidOrganisationGroup() {
    when(portalOrganisationsAccessor.getAllOrganisationGroupsWithUrefIn(any())).thenReturn(List.of());
    pwaTeamsDtoFactory.createOrganisationTeam(organisationTeamDto1);
  }

  @Test(expected = PwaTeamFactoryException.class)
  public void createOrganisationTeam_errorsWhenAssociatedUrefNotScoped() {
    when(portalOrganisationsAccessor.getAllOrganisationGroupsWithUrefIn(any())).thenReturn(List.of());
    organisationTeamDto1 = new PortalTeamDto(1, "NAME", "DESC", PwaTeamType.ORGANISATION.getPortalTeamType(), null);

    pwaTeamsDtoFactory.createOrganisationTeam(organisationTeamDto1);
  }

  @Test
  public void createOrganisationTeamList_verifyOrganisationsRetrievedInOneHit() {
    List<String> expectedUrefList = List.of(portalOrganisationGroup1.getUrefValue(), portalOrganisationGroup2.getUrefValue());
    when(portalOrganisationsAccessor.getAllOrganisationGroupsWithUrefIn(any()))
        .thenReturn(List.of(portalOrganisationGroup1, portalOrganisationGroup2));

    pwaTeamsDtoFactory.createOrganisationTeamList(List.of(organisationTeamDto1, organisationTeamDto2));
    verify(portalOrganisationsAccessor, times(1)).getAllOrganisationGroupsWithUrefIn(eq(expectedUrefList));
    verifyNoMoreInteractions(portalOrganisationsAccessor);
  }

  @Test
  public void createPwaTeamMemberList_verifyTeamMemberPersonsGotInOneHit() {
    PortalTeamMemberDto orgTeamMember1 = TeamTestingUtils.createPortalTeamMember(orgMember1, organisationTeam1);
    PortalTeamMemberDto orgTeamMember2 = TeamTestingUtils.createPortalTeamMember(orgMember2, organisationTeam1);
    List<PortalTeamMemberDto> portalTeamMemberDtos = List.of(orgTeamMember1, orgTeamMember2);

    List<PwaTeamMember> pwaTeamMembers = pwaTeamsDtoFactory.createPwaTeamMemberList(portalTeamMemberDtos, organisationTeam1);

    assertThat(pwaTeamMembers).hasSize(2);
    verify(personRepository, times(1))
        .findAllByIdIn(eq(Set.of(orgMember1.getId().asInt(), orgMember2.getId().asInt())));
  }

  @Test
  public void createPwaTeamMember_mapsDtoPropertiesAsExpected() {
    PwaTeamMember teamMember = pwaTeamsDtoFactory.createPwaTeamMember(orgTeamMember1, orgMember1, organisationTeam1);

    assertThat(teamMember.getPerson()).isEqualTo(orgMember1);
    assertThat(teamMember.getTeam()).isEqualTo(organisationTeam1);
  }

  @Test
  public void createPwaTeamMember_mapsRolesAsExpected() {
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
  public void createPwaUserPrivilegeList_mapsPrivAsExpected_andRemovesDuplicates() {
    List<PwaUserPrivilege> privs = pwaTeamsDtoFactory.createPwaUserPrivilegeList(List.of(
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
