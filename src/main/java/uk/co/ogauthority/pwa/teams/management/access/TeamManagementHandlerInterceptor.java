package uk.co.ogauthority.pwa.teams.management.access;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;
import uk.co.ogauthority.pwa.util.SecurityUtils;

@Component
public class TeamManagementHandlerInterceptor implements HandlerInterceptor {

  private final TeamManagementService teamManagementService;
  private final TeamQueryService teamQueryService;

  public TeamManagementHandlerInterceptor(TeamManagementService teamManagementService,
                                          TeamQueryService teamQueryService) {
    this.teamManagementService = teamManagementService;
    this.teamQueryService = teamQueryService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {
    if (handler instanceof ResourceHttpRequestHandler) {
      return true;
    }

    Optional<AuthenticatedUserAccount> possibleUser = SecurityUtils.getAuthenticatedUserFromSecurityContext();
    if (possibleUser.isEmpty()) {
      return false;
    }

    if (handler instanceof HandlerMethod handlerMethod) {

      var wuaId = (long) possibleUser.get().getWuaId();

      if (hasAnnotation(handlerMethod, InvokingUserCanManageTeam.class)) {
        return handleInvokingUserCanManageTeamCheck(request, wuaId);
      }

      if (hasAnnotation(handlerMethod, InvokingUserHasStaticRole.class)) {
        var annotation = getAnnotation(handlerMethod, InvokingUserHasStaticRole.class);
        return handleInvokingUserHasRoleCheck(wuaId, annotation.teamType(), annotation.role());
      }

      if (hasAnnotation(handlerMethod, InvokingUserCanViewTeam.class)) {
        return handleInvokingUserCanViewTeam(request, wuaId);
      }

      return true;
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unexpected handler class %s".formatted(handler.getClass()));
  }

  private boolean handleInvokingUserCanManageTeamCheck(HttpServletRequest request, Long wuaId) {

    var team = getTeamFromRequest(request);

    var isScoped = team.getTeamType().isScoped();
    boolean canManageTeam;
    if (isScoped) {
      canManageTeam = teamManagementService.getScopedTeamsOfTypeUserCanManage(team.getTeamType(), wuaId).contains(team);
    } else {
      canManageTeam = teamManagementService.getStaticTeamOfTypeUserCanManage(team.getTeamType(), wuaId).map(
          t -> t.equals(team)).isPresent();
    }

    if (canManageTeam) {
      return true;
    } else {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "wuaId %s does not have manage team role for teamId %s".formatted(wuaId, team.getId()));
    }
  }

  private boolean handleInvokingUserHasRoleCheck(Long wuaId, TeamType teamType, Role role) {
    if (teamQueryService.userHasStaticRole(wuaId, teamType, role)) {
      return true;
    } else {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "wuaId %s does not have static role %s for teamType %s".formatted(wuaId, role, teamType)
      );
    }
  }

  private boolean hasAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> annotation) {
    return AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation) != null
        || AnnotationUtils.findAnnotation(handlerMethod.getMethod().getDeclaringClass(), annotation) != null;
  }

  private  <T extends Annotation> T getAnnotation(HandlerMethod handlerMethod, Class<T> annotation) {
    return Objects.requireNonNullElse(
        AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation),
        AnnotationUtils.findAnnotation(handlerMethod.getMethod().getDeclaringClass(), annotation)
    );
  }

  private boolean handleInvokingUserCanViewTeam(HttpServletRequest request, Long wuaId) {

    var team = getTeamFromRequest(request);

    if (teamManagementService.isMemberOfTeam(team, wuaId)
        || canManageAnyOrgTeam(wuaId, team)
        || canManageAnyConsulteeGroupTeam(wuaId, team)
    ) {
      return true;
    }

    throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "wuaId %s is not a member of team %s".formatted(wuaId, team.getId())
    );
  }

  private boolean canManageAnyOrgTeam(Long wuaId, Team team) {
    return TeamType.ORGANISATION.equals(team.getTeamType())
        && teamManagementService.userCanManageAnyOrganisationTeam(wuaId);
  }

  private boolean canManageAnyConsulteeGroupTeam(Long wuaId, Team team) {
    return TeamType.CONSULTEE.equals(team.getTeamType())
        && teamManagementService.userCanManageAnyConsulteeGroupTeam(wuaId);
  }

  @SuppressWarnings("unchecked")
  private Team getTeamFromRequest(HttpServletRequest request) {

    var pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    var teamIdString = pathVariables.get("teamId");

    if (teamIdString == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teamId path variable not found in request");
    }

    UUID teamId;

    try {
      teamId = UUID.fromString(teamIdString);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UUID parse error", e);
    }

    return teamManagementService.getTeam(teamId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "teamId %s not found".formatted(teamId)));
  }
}