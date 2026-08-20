package uk.co.ogauthority.pwa.teams.management;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamDto;
import uk.co.fivium.energyportal.starter.accounts.EnergyPortalServiceAccessService;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalAccountsMessagePublishingService;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.UsersProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.ogauthority.pwa.config.ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.epa.correlationid.CorrelationIdUtil;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamMemberQueryService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamRepository;
import uk.co.ogauthority.pwa.teams.TeamRole;
import uk.co.ogauthority.pwa.teams.TeamRoleRepository;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

@Service
public class TeamManagementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TeamManagementService.class);

  private static final String TEAM_TYPE_SCOPED_EXCEPTION_MESSAGE = "TeamType %s is scoped, expected static";
  private static final String TEAM_TYPE_STATIC_EXCEPTION_MESSAGE = "TeamType %s is static, expected scoped";

  private final TeamRepository teamRepository;
  private final TeamRoleRepository teamRoleRepository;
  private final TeamQueryService teamQueryService;
  private final UserApi userApi;
  private final TeamMemberQueryService teamMemberQueryService;
  private final PwaContactRepository pwaContactRepository;
  private final UserAccountService userAccountService;
  private final EnergyPortalServiceAccessService energyPortalServiceAccessService;
  private final EnergyPortalAccountsMessagePublishingService energyPortalAccountsMessagePublishingService;
  private final ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties consulteeConfigurationProperties;

  public TeamManagementService(
      TeamRepository teamRepository,
      TeamRoleRepository teamRoleRepository,
      UserApi userApi,
      TeamQueryService teamQueryService,
      TeamMemberQueryService teamMemberQueryService,
      PwaContactRepository pwaContactRepository,
      UserAccountService userAccountService,
      EnergyPortalServiceAccessService energyPortalServiceAccessService,
      EnergyPortalAccountsMessagePublishingService energyPortalAccountsMessagePublishingService,
      ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties configProperties
  ) {
    this.teamRepository = teamRepository;
    this.teamRoleRepository = teamRoleRepository;
    this.userApi = userApi;
    this.teamQueryService = teamQueryService;
    this.teamMemberQueryService = teamMemberQueryService;
    this.pwaContactRepository = pwaContactRepository;
    this.userAccountService = userAccountService;
    this.energyPortalServiceAccessService = energyPortalServiceAccessService;
    this.energyPortalAccountsMessagePublishingService = energyPortalAccountsMessagePublishingService;
    consulteeConfigurationProperties = configProperties;
  }

  public Team createScopedTeam(String name, TeamType teamType, TeamScopeReference scopeRef) {
    if (!teamType.isScoped()) {
      throw new TeamManagementException("Team of type %s is not scoped".formatted(teamType));
    }

    if (doesScopedTeamWithReferenceExist(teamType, scopeRef)) {
      throw new TeamManagementException("Team of type %s scope type %s and scope id %s already exists"
          .formatted(teamType, scopeRef.getId(), scopeRef.getType()));
    }

    var team = new Team();
    team.setName(name);
    team.setTeamType(teamType);
    team.setScopeType(scopeRef.getType());
    team.setScopeId(scopeRef.getId());
    team = teamRepository.save(team);

    if (TeamType.CONSULTEE.equals(teamType)
        && !consulteeConfigurationProperties.hasAssociatedEpas(team.getScopeId())) {
      LOGGER.warn(
          "New consultee team with scope id {} has been created, and EPAS has not been notified, due to no associated org group",
          scopeRef.getId()
      );
      return team;
    }

    var scopeType = ScopeType.ORGANISATION_GROUP;
    var scopeId = team.getScopeId();

    if (TeamType.CONSULTEE.equals(team.getTeamType())) {
      var scopeTypeAndId = consulteeConfigurationProperties.getScopeTypeAndEpasScopeId(team.getScopeId());
      scopeType = scopeTypeAndId.scopeType();
      scopeId = scopeTypeAndId.scopeId().toString();
    }

    var serviceProviderTeam = new ServiceProviderTeamDto(
        team.getId().toString(),
        scopeId,
        scopeType,
        team.getTeamType().name()
    );
    energyPortalAccountsMessagePublishingService.publishTeam(serviceProviderTeam);

    return team;
  }

  public void updateTeamName(Team team, String teamName) {
    team.setName(teamName);

    teamRepository.save(team);
  }

  Set<TeamType> getTeamTypesUserIsMemberOf(long wuaId) {
    return teamRoleRepository.findAllByWuaId(wuaId)
        .stream()
        .map(teamRole -> teamRole.getTeam().getTeamType())
        .collect(Collectors.toSet());
  }

  public Optional<Team> getStaticTeamOfTypeUserCanManage(TeamType teamType, Long wuaId) {
    if (teamType.isScoped()) {
      throw new TeamManagementException(TEAM_TYPE_SCOPED_EXCEPTION_MESSAGE.formatted(teamType));
    }

    List<Team> teams = new ArrayList<>();
    addStaticTeamsUserCanManage(teams, teamType, wuaId);
    teams.addAll(getTeamsOfTypeUserCanManage(teamType, wuaId));

    return teams
        .stream()
        .distinct()
        .findFirst();
  }

  private void addStaticTeamsUserCanManage(Collection<Team> teams, TeamType teamType, Long wuaId) {
    if (TeamType.SECONDARY_REGULATOR.equals(teamType) && userCanManageSecondaryRegulatorTeam(wuaId)) {
      teams.addAll(teamRepository.findByTeamType(TeamType.SECONDARY_REGULATOR));
    }
  }

  public Optional<Team> getStaticTeamOfTypeUserCanView(TeamType teamType, Long wuaId) {
    if (teamType.isScoped()) {
      throw new TeamManagementException(TEAM_TYPE_SCOPED_EXCEPTION_MESSAGE.formatted(teamType));
    }
    List<Team> teams = new ArrayList<>();
    addStaticTeamsUserCanManage(teams, teamType, wuaId);
    teams.addAll(getTeamsOfTypeUserIsMemberOf(teamType, wuaId));

    return teams
        .stream()
        .filter(team -> teamType.equals(team.getTeamType()))
        .findFirst();
  }

  Optional<Team> getStaticTeamOfTypeUserIsMemberOf(TeamType teamType, Long wuaId) {
    if (teamType.isScoped()) {
      throw new TeamManagementException(TEAM_TYPE_SCOPED_EXCEPTION_MESSAGE.formatted(teamType));
    }
    return getTeamsOfTypeUserIsMemberOf(teamType, wuaId)
        .stream()
        .findFirst();
  }

  public List<Team> getScopedTeamsOfTypeUserCanManage(TeamType teamType, Long wuaId) {
    if (!teamType.isScoped()) {
      throw new TeamManagementException(TEAM_TYPE_STATIC_EXCEPTION_MESSAGE.formatted(teamType));
    }
    var teams = new ArrayList<>(getTeamsOfTypeUserCanManage(teamType, wuaId));

    addScopedTeamTypesUserCanManage(teams, teamType, wuaId);

    return teams.stream()
        .distinct() // Remove possible dupes from adding all scoped teams the user may already be a team manager of
        .toList();
  }

  Set<Team> getScopedTeamsOfTypeUserIsMemberOf(TeamType teamType, Long wuaId) {

    if (!teamType.isScoped()) {
      throw new TeamManagementException(TEAM_TYPE_STATIC_EXCEPTION_MESSAGE.formatted(teamType));
    }

    var teams = new HashSet<>(getTeamsOfTypeUserIsMemberOf(teamType, wuaId));

    addScopedTeamTypesUserCanManage(teams, teamType, wuaId);

    return new HashSet<>(teams);
  }

  private void addScopedTeamTypesUserCanManage(Collection<Team> teams, TeamType teamType, Long wuaId) {
    if (teamType.equals(TeamType.ORGANISATION) && userCanManageAnyOrganisationTeam(wuaId)) {
      // If we want org teams, and the user is a regulator who can manage any org team, include all the org teams.
      teams.addAll(getAllScopedTeamsOfType(TeamType.ORGANISATION));
    }

    if (teamType.equals(TeamType.CONSULTEE) && userCanManageAnyConsulteeGroupTeam(wuaId)) {
      // If we want consultee groups, and the user is a regulator who can manage any consultee group team,
      // include all the consultee group teams.
      teams.addAll(getAllScopedTeamsOfType(TeamType.CONSULTEE));
    }
  }

  public Optional<Team> getTeam(UUID teamId) {
    return teamRepository.findById(teamId);
  }

  public Optional<Team> getScopedTeam(TeamType teamType, TeamScopeReference scopeRef) {
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId());
  }

  public Optional<User> getEnergyPortalUser(String emailAddress) {
    var projection = new UsersProjectionRoot()
        .webUserAccountId()
        .isAccountShared()
        .canLogin();

    var users = userApi.searchUsersByEmail(
        emailAddress,
        projection,
        new RequestPurpose("Find user to add to team"),
        CorrelationIdUtil.getLogCorrelationId());

    if (users.size() > 1) {
      throw new TeamManagementException(
          "More than one UK Energy Portal user exists with the email address %s".formatted(emailAddress)
      );
    }

    return users
        .stream()
        .findFirst();
  }

  TeamMemberView getTeamMemberView(Team team, Long wuaId) {

    return teamMemberQueryService.getTeamMemberView(team, wuaId);
  }

  List<TeamMemberView> getTeamMemberViewsForTeam(Team team) {

    return teamMemberQueryService.getTeamMemberViewsForTeam(team);
  }

  /**
   * This sets the roles for a given user and team. It also validates:
   * - The given roles are valid for the team type of the given team.
   * - A user exists for the given wuaId
   * - The given wuaId is active.
   * It does not, however, validate that a team has at least one access manager after the roles have been updated. Consumers
   * should validate that before calling this method, unless calling it in response to an EPAS EPMQ message
   *
   * @param wuaId The wuaId of the user who's roles we want to update
   * @param team  The team which the user roles are being set for.
   * @param roles The roles to assign to the user.
   */
  @Transactional
  public void setUserTeamRoles(Long wuaId, Team team, List<Role> roles, Long instigatingWuaId) {
    if (!new HashSet<>(team.getTeamType().getAllowedRoles()).containsAll(roles)) {
      throw new TeamManagementException("Roles %s are not valid for team type %s".formatted(roles, team.getTeamType()));
    }

    // Check the user is valid
    var projection = new UserProjectionRoot()
        .isAccountShared()
        .canLogin();
    var userOptional = userApi.findUserById(
        Math.toIntExact(wuaId),
        projection,
        new RequestPurpose("Validate user account"),
        CorrelationIdUtil.getLogCorrelationId()
    );
    if (userOptional.isEmpty()) {
      throw new TeamManagementException("User account with wuaId %s does not exist".formatted(wuaId));
    }
    var user = userOptional.get();
    if (user.getIsAccountShared()) {
      throw new TeamManagementException(
          "User account with wuaId %s is a shared account so can't be added to teams".formatted(wuaId));
    }
    if (!user.getCanLogin()) {
      throw new TeamManagementException("User account with wuaId %s is not active so can't be added to teams".formatted(wuaId));
    }

    var isNewUser = userNotInAnyTeam(wuaId);

    teamRoleRepository.deleteByWuaIdAndTeam(wuaId, team);

    var newTeamRoles = roles.stream()
        .map(role -> {
          var teamRole = new TeamRole();
          teamRole.setTeam(team);
          teamRole.setRole(role);
          teamRole.setWuaId(wuaId);
          return teamRole;
        }).toList();
    teamRoleRepository.saveAll(newTeamRoles);

    energyPortalAccountsMessagePublishingService.publishUsersRolesForTeam(
        wuaId,
        team.getId().toString(),
        team.getTeamType().name(),
        roles.stream().map(Role::name).collect(Collectors.toSet())
    );

    if (!isNewUser) {
      return;
    }
    energyPortalServiceAccessService.addUser(wuaId);
  }

  @Transactional
  public void removeUserFromTeam(Long wuaId, Team team) {
    if (!willManageTeamRoleBePresentAfterMemberRemoval(team, wuaId)) {
      throw new TeamManagementException("Can't remove last team manager user %s from team %s".formatted(wuaId, team.getId()));
    }
    teamRoleRepository.deleteByWuaIdAndTeam(wuaId, team);

    energyPortalAccountsMessagePublishingService.publishRemoveUserFromTeam(
        wuaId,
        team.getId().toString()
    );

    if (!teamRoleRepository.findAllByWuaId(wuaId).isEmpty()) {
      return;
    }
    energyPortalServiceAccessService.removeUser(wuaId);
  }

  private boolean userNotInAnyTeam(Long wuaId) {
    var user = userAccountService.getWebUserAccount(wuaId.intValue());
    return teamRoleRepository.findAllByWuaId(wuaId).isEmpty()
        && !pwaContactRepository.existsByPerson(user.getLinkedPerson());
  }

  public boolean willManageTeamRoleBePresentAfterMemberRoleUpdate(Team team, Long wuaId, List<Role> membersNewRoles) {
    if (membersNewRoles.contains(Role.TEAM_ADMINISTRATOR)) {
      return true;
    }
    return willManageTeamRoleBePresentAfterMemberRemoval(team, wuaId);
  }

  public boolean willManageTeamRoleBePresentAfterMemberRemoval(Team team, Long wuaId) {
    return teamRoleRepository.findByTeam(team).stream()
        .filter(teamRole -> !teamRole.getWuaId().equals(wuaId))
        .anyMatch(teamRole -> teamRole.getRole().equals(Role.TEAM_ADMINISTRATOR));
  }

  public boolean doesScopedTeamWithReferenceExist(TeamType teamType, TeamScopeReference scopeRef) {
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId())
        .isPresent();
  }

  public boolean canManageTeam(Team team, long wuaId) {
    if (team.getTeamType().isScoped()) {
      return getScopedTeamsOfTypeUserCanManage(team.getTeamType(), wuaId)
          .stream()
          .anyMatch(scopedTeam -> scopedTeam.getId().equals(team.getId()));
    } else {
      return getStaticTeamOfTypeUserCanManage(team.getTeamType(), wuaId).isPresent();
    }
  }

  public boolean canAddUserToTeam(long wuaId, Team team) {
    var teamType = team.getTeamType();

    if (!teamType.isScoped()) {
      return true;
    }

    return switch (teamType.getUserMembershipRestriction()) {
      case SINGLE_TEAM -> !isMemberOfAnyTeamInTeamType(wuaId, teamType);
      case MULTIPLE_TEAMS -> true;
    };
  }

  private boolean isMemberOfAnyTeamInTeamType(long wuaId, TeamType teamType) {
    return !getScopedTeamsOfTypeUserIsMemberOf(teamType, wuaId).isEmpty();
  }

  public boolean isMemberOfTeam(Team team, long wuaId) {
    return teamRoleRepository.existsByTeamAndWuaId(team, wuaId);
  }

  public boolean userCanManageAnyOrganisationTeam(long wuaId) {
    return teamQueryService.userHasStaticRole(wuaId, TeamType.REGULATOR, Role.ORGANISATION_MANAGER);
  }

  public boolean userCanManageAnyConsulteeGroupTeam(long wuaId) {
    return teamQueryService.userHasStaticRole(wuaId, TeamType.REGULATOR, Role.CONSULTEE_GROUP_MANAGER);
  }

  public boolean userCanManageSecondaryRegulatorTeam(long wuaId) {
    return teamQueryService.userHasStaticRole(wuaId, TeamType.REGULATOR, Role.SECONDARY_REGULATOR_MANAGER);
  }

  private List<Team> getAllScopedTeamsOfType(TeamType teamType) {
    if (!teamType.isScoped()) {
      throw new TeamManagementException(TEAM_TYPE_STATIC_EXCEPTION_MESSAGE.formatted(teamType));
    }
    return teamRepository.findByTeamType(teamType);
  }

  private List<Team> getTeamsUserCanManage(Long wuaId) {
    var userTeamRoles = teamRoleRepository.findByWuaIdAndRole(wuaId, Role.TEAM_ADMINISTRATOR);
    return userTeamRoles.stream()
        .map(TeamRole::getTeam)
        .toList();
  }

  private Set<Team> getTeamsUserIsMemberOf(Long wuaId) {
    var userTeamRoles = teamRoleRepository.findAllByWuaId(wuaId);
    return userTeamRoles.stream()
        .map(TeamRole::getTeam)
        .collect(Collectors.toSet());
  }

  private List<Team> getTeamsOfTypeUserCanManage(TeamType teamType, Long wuaId) {
    return getTeamsUserCanManage(wuaId).stream()
        .filter(team -> team.getTeamType().equals(teamType))
        .toList();
  }

  private Set<Team> getTeamsOfTypeUserIsMemberOf(TeamType teamType, Long wuaId) {
    return getTeamsUserIsMemberOf(wuaId).stream()
        .filter(team -> team.getTeamType().equals(teamType))
        .collect(Collectors.toSet());
  }
}