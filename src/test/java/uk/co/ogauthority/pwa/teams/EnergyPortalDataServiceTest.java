package uk.co.ogauthority.pwa.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamDto;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamTypeRoleDto;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderUserTeamRolesDto;
import uk.co.ogauthority.pwa.config.ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class EnergyPortalDataServiceTest {

  private static final ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties configProperties
      = new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties(Map.of(
      1, new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties.ScopeTypeAndIdDto(
          ScopeType.ORGANISATION_GROUP,
          111
      ),
      2, new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties.ScopeTypeAndIdDto(
          ScopeType.ORGANISATION_UNIT,
          222
      ),
      3, new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties.ScopeTypeAndIdDto(
          ScopeType.ORGANISATION_UNIT,
          133311
      )
  ));

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamRoleRepository teamRoleRepository;

  private EnergyPortalDataService energyPortalDataService;

  @BeforeEach
  void setUp() {
    energyPortalDataService = new EnergyPortalDataService(teamRepository, teamRoleRepository, configProperties);
  }

  @Test
  void getServiceProviderTeamDtos() {
    var team1 = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.REGULATOR)
        .withScopeType(TeamType.REGULATOR.getScopeType())
        .build();
    var team2 = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.ORGANISATION)
        .withScopeType(TeamType.ORGANISATION.getScopeType())
        .build();
    var consulteeTeamWhichHasAssociatedOrgGroup = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.CONSULTEE)
        .withName("Consultee name")
        .withScopeId("2")
        .withScopeType(TeamType.CONSULTEE.getScopeType())
        .build();
    var consulteeTeamWhichHasNoAssociatedOrgGroup = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.CONSULTEE)
        .withScopeId("no associated org group")
        .withScopeType(TeamType.CONSULTEE.getScopeType())
        .build();
    var secondaryRegulatorTeam = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.SECONDARY_REGULATOR)
        .withScopeType(TeamType.SECONDARY_REGULATOR.getScopeType())
        .build();

    var expectedDto1 = new ServiceProviderTeamDto(
        team1.getId().toString(),
        team1.getScopeId(),
        ScopeType.ORGANISATION_GROUP,
        null,
        team1.getTeamType().name()
    );
    var expectedDto2 = new ServiceProviderTeamDto(
        team2.getId().toString(),
        team2.getScopeId(),
        ScopeType.ORGANISATION_GROUP,
        null,
        team2.getTeamType().name()
    );
    var expectedDto3 = new ServiceProviderTeamDto(
        consulteeTeamWhichHasAssociatedOrgGroup.getId().toString(),
        configProperties.consulteeGroupIdToScopeIdAndType().get(2).scopeId().toString(),
        configProperties.consulteeGroupIdToScopeIdAndType().get(2).scopeType(),
        "Consultee name",
        consulteeTeamWhichHasAssociatedOrgGroup.getTeamType().name()
    );
    var expectedDto4 = new ServiceProviderTeamDto(
        secondaryRegulatorTeam.getId().toString(),
        secondaryRegulatorTeam.getScopeId(),
        ScopeType.ORGANISATION_GROUP,
        null,
        secondaryRegulatorTeam.getTeamType().name()
    );

    when(teamRepository.findAll()).thenReturn(List.of(
        team1,
        team2,
        consulteeTeamWhichHasAssociatedOrgGroup,
        consulteeTeamWhichHasNoAssociatedOrgGroup,
        secondaryRegulatorTeam
    ));

    assertThat(energyPortalDataService.getServiceProviderTeamDtos())
        .containsExactlyInAnyOrder(expectedDto1, expectedDto2, expectedDto3, expectedDto4);
  }

  @Test
  void getTeamTypeToServiceProviderTeamTypeRoleDtos() {
    var regulatorServiceRoleDtos = Set.of(
        createServiceRoleDto(Role.TEAM_ADMINISTRATOR, true),
        createServiceRoleDto(Role.ORGANISATION_MANAGER, false),
        createServiceRoleDto(Role.CONSULTEE_GROUP_MANAGER, false),
        createServiceRoleDto(Role.PWA_MANAGER, false),
        createServiceRoleDto(Role.CASE_OFFICER, false),
        createServiceRoleDto(Role.CONSENT_VIEWER, false),
        createServiceRoleDto(Role.AS_BUILT_NOTIFICATION_ADMIN, false),
        createServiceRoleDto(Role.TEMPLATE_CLAUSE_MANAGER, false)
    );

    var consulteeServiceRoleDtos = Set.of(
        createServiceRoleDto(Role.TEAM_ADMINISTRATOR, true),
        createServiceRoleDto(Role.RECIPIENT, false),
        createServiceRoleDto(Role.RESPONDER, false)
    );

    var organisationServiceRoleDtos = Set.of(
        createServiceRoleDto(Role.TEAM_ADMINISTRATOR, true),
        createServiceRoleDto(Role.APPLICATION_CREATOR, false),
        createServiceRoleDto(Role.APPLICATION_SUBMITTER, false),
        createServiceRoleDto(Role.FINANCE_ADMIN, false),
        createServiceRoleDto(Role.AS_BUILT_NOTIFICATION_SUBMITTER, false)
    );

    var secondaryRegulatorServiceRoleDtos = Set.of(
        createServiceRoleDto(Role.TEAM_ADMINISTRATOR, true),
        createServiceRoleDto(Role.CONSENT_VIEWER, false)
    );

    assertThat(energyPortalDataService.getTeamTypeToServiceProviderTeamTypeRoleDtos())
        .isEqualTo(
            Map.of(
                TeamType.REGULATOR.name(), regulatorServiceRoleDtos,
                TeamType.CONSULTEE.name(), consulteeServiceRoleDtos,
                TeamType.ORGANISATION.name(), organisationServiceRoleDtos,
                TeamType.SECONDARY_REGULATOR.name(), secondaryRegulatorServiceRoleDtos
            )
        );
  }

  @Test
  void getTeamTypes() {
    assertThat(energyPortalDataService.getTeamTypes()).isEqualTo(Set.of(
        TeamType.REGULATOR.name(),
        TeamType.CONSULTEE.name(),
        TeamType.ORGANISATION.name(),
        TeamType.SECONDARY_REGULATOR.name()
    ));
  }

  @Test
  void getServiceProviderUserTeamRolesDtos() {
    var team1 = TeamTestUtil.newBuilder().build();
    var team2 = TeamTestUtil.newBuilder().build();

    var wuaId1Team1TeamRole = TeamRoleTestUtil.newBuilder()
        .withWuaId(1L)
        .withTeam(team1)
        .withRole(Role.TEAM_ADMINISTRATOR)
        .build();

    var wuaId1Team2TeamRole = TeamRoleTestUtil.newBuilder()
        .withWuaId(1L)
        .withTeam(team2)
        .withRole(Role.ORGANISATION_MANAGER)
        .build();

    var wuaId2Team2TeamRole1 = TeamRoleTestUtil.newBuilder()
        .withWuaId(2L)
        .withTeam(team2)
        .withRole(Role.TEAM_ADMINISTRATOR)
        .build();

    var wuaId2Team2TeamRole2 = TeamRoleTestUtil.newBuilder()
        .withWuaId(2L)
        .withTeam(team2)
        .withRole(Role.APPLICATION_CREATOR)
        .build();

    when(teamRoleRepository.findAll()).thenReturn(List.of(
        wuaId1Team1TeamRole,
        wuaId1Team2TeamRole,
        wuaId2Team2TeamRole1,
        wuaId2Team2TeamRole2
    ));

    assertThat(energyPortalDataService.getServiceProviderUserTeamRolesDtos())
        .containsExactlyInAnyOrder(
            new ServiceProviderUserTeamRolesDto(
                1L,
                team1.getId().toString(),
                team1.getTeamType().name(),
                Set.of(Role.TEAM_ADMINISTRATOR.name())
            ),
            new ServiceProviderUserTeamRolesDto(
                1L,
                team2.getId().toString(),
                team2.getTeamType().name(),
                Set.of(Role.ORGANISATION_MANAGER.name())
            ),
            new ServiceProviderUserTeamRolesDto(
                2L,
                team2.getId().toString(),
                team2.getTeamType().name(),
                Set.of(Role.TEAM_ADMINISTRATOR.name(), Role.APPLICATION_CREATOR.name())
            )
        );
  }

  @Test
  void belongsToAnyTeam_returnsTrue_whenUserHasTeamRoles() {
    when(teamRoleRepository.existsByWuaId(300165L)).thenReturn(true);

    assertThat(energyPortalDataService.belongsToAnyTeam(300165L)).isTrue();
  }

  @Test
  void belongsToAnyTeam_returnsFalse_whenUserHasNoTeamRoles() {
    when(teamRoleRepository.existsByWuaId(300165L)).thenReturn(false);

    assertThat(energyPortalDataService.belongsToAnyTeam(300165L)).isFalse();
  }

  private ServiceProviderTeamTypeRoleDto createServiceRoleDto(Role role, boolean isAssessManager) {
    return new ServiceProviderTeamTypeRoleDto(
        role.name(),
        role.getName(),
        role.getDescription(),
        isAssessManager,
        role.ordinal()
    );
  }
}