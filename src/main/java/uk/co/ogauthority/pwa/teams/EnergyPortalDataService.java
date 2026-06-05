package uk.co.ogauthority.pwa.teams;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamDto;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamTypeRoleDto;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderUserTeamRolesDto;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderDataService;
import uk.co.ogauthority.pwa.config.ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties;

@Service
class EnergyPortalDataService implements EnergyPortalServiceProviderDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnergyPortalDataService.class);

  private final TeamRepository teamRepository;
  private final TeamRoleRepository teamRoleRepository;
  private final ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties consulteeConfigurationProperties;

  EnergyPortalDataService(
      TeamRepository teamRepository,
      TeamRoleRepository teamRoleRepository,
      ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties configProperties
  ) {
    this.teamRepository = teamRepository;
    this.teamRoleRepository = teamRoleRepository;
    this.consulteeConfigurationProperties = configProperties;
  }

  @Override
  public Collection<ServiceProviderTeamDto> getServiceProviderTeamDtos() {
    var serviceProviderTeamDtos = new HashSet<ServiceProviderTeamDto>();

    for (var team : teamRepository.findAll()) {
      if (TeamType.CONSULTEE.equals(team.getTeamType())
          && !consulteeConfigurationProperties.hasAssociatedEpas(team.getScopeId())) {
        LOGGER.warn("Consultee team {} with id {} not sent to EPAS, as no organisation group exists for it.",
            team.getId(),
            team.getScopeId()
        );
        continue;
      }

      var scopeType = ScopeType.ORGANISATION_GROUP;
      var scopeId = team.getScopeId();
      String overrideName = null;

      if (TeamType.CONSULTEE.equals(team.getTeamType())) {
        var scopeTypeAndId = consulteeConfigurationProperties.getScopeTypeAndEpasScopeId(team.getScopeId());
        scopeType = scopeTypeAndId.scopeType();
        scopeId = scopeTypeAndId.scopeId().toString();
        overrideName = team.getName();
      }

      serviceProviderTeamDtos.add(new ServiceProviderTeamDto(
          team.getId().toString(),
          scopeId,
          scopeType,
          overrideName,
          team.getTeamType().name()
      ));
    }
    return serviceProviderTeamDtos;
  }

  @Override
  public Map<String, Collection<ServiceProviderTeamTypeRoleDto>> getTeamTypeToServiceProviderTeamTypeRoleDtos() {
    Map<String, Collection<ServiceProviderTeamTypeRoleDto>> teamTypeToRoles = new HashMap<>();

    for (var teamType : TeamType.values()) {
      var serviceRoleDtos = teamType.getAllowedRoles()
          .stream()
          .map(role -> new ServiceProviderTeamTypeRoleDto(
                  role.name(),
                  role.getName(),
                  role.getDescription(),
                  role == Role.TEAM_ADMINISTRATOR,
                  role.ordinal()
              )
          ).collect(Collectors.toSet());

      teamTypeToRoles.put(teamType.name(), serviceRoleDtos);
    }

    return teamTypeToRoles;
  }

  @Override
  public Collection<String> getTeamTypes() {
    return Arrays.stream(TeamType.values())
        .map(TeamType::name)
        .collect(Collectors.toSet());
  }

  @Override
  public Collection<ServiceProviderUserTeamRolesDto> getServiceProviderUserTeamRolesDtos() {
    var serviceProviderUserTeamRolesDtos = new HashSet<ServiceProviderUserTeamRolesDto>();

    var wuaIdToTeamRoles = teamRoleRepository.findAll()
        .stream()
        .collect(groupingBy(TeamRole::getWuaId, toSet()));

    for (var wuaIdToTeamRoleEntry : wuaIdToTeamRoles.entrySet()) {
      var teamToTeamRoles = wuaIdToTeamRoleEntry.getValue()
          .stream()
          .collect(groupingBy(TeamRole::getTeam, toSet()));

      for (var teamToTeamRoleEntry : teamToTeamRoles.entrySet()) {
        var wuaId = wuaIdToTeamRoleEntry.getKey();
        var team = teamToTeamRoleEntry.getKey();
        var roles = teamToTeamRoleEntry.getValue()
            .stream()
            .map(teamRole -> teamRole.getRole().name())
            .collect(Collectors.toSet());

        serviceProviderUserTeamRolesDtos.add(new ServiceProviderUserTeamRolesDto(
            wuaId,
            team.getId().toString(),
            team.getTeamType().name(),
            roles
        ));
      }
    }
    return serviceProviderUserTeamRolesDtos;
  }

  @Override
  public boolean belongsToAnyTeam(long wuaId) {
    return teamRoleRepository.existsByWuaId(wuaId);
  }
}