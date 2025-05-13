package uk.co.ogauthority.pwa.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.management.ScopedTeamManagementController;

public enum TeamType {

  REGULATOR(
      "Regulator",
      "regulator",
      null,
      List.of(
          Role.TEAM_ADMINISTRATOR,
          Role.ORGANISATION_MANAGER,
          Role.CONSULTEE_GROUP_MANAGER,
          Role.PWA_MANAGER,
          Role.CASE_OFFICER,
          Role.CONSENT_VIEWER,
          Role.AS_BUILT_NOTIFICATION_ADMIN,
          Role.TEMPLATE_CLAUSE_MANAGER
      ),
      null,
      UserMembershipRestriction.SINGLE_TEAM
  ),
  CONSULTEE(
      "Consultee Groups",
      "consultee",
      "CONSULTEE",
      List.of(
          Role.TEAM_ADMINISTRATOR,
          Role.RECIPIENT,
          Role.RESPONDER
      ),
          () -> ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewConsulteeGroupTeam(null)),
      UserMembershipRestriction.SINGLE_TEAM
  ),
  ORGANISATION(
      "Organisations",
      "organisation",
      "ORGGRP",
      List.of(
          Role.TEAM_ADMINISTRATOR,
          Role.APPLICATION_CREATOR,
          Role.APPLICATION_SUBMITTER,
          Role.FINANCE_ADMIN,
          Role.AS_BUILT_NOTIFICATION_SUBMITTER
      ),
          () -> ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewOrgTeam(null)),
      UserMembershipRestriction.MULTIPLE_TEAMS
  ),
  ;

  public enum UserMembershipRestriction { SINGLE_TEAM, MULTIPLE_TEAMS }

  private final String displayName;
  private final String urlSlug;
  private final boolean isScoped;
  private final String scopeType;
  private final List<Role> allowedRoles;
  private final Supplier<String> createNewInstanceRoute;
  private final UserMembershipRestriction userMembershipRestriction;

  TeamType(String displayName,
           String urlSlug,
           String scopeType,
           List<Role> allowedRoles,
           Supplier<String> createNewInstanceRoute,
           UserMembershipRestriction userMembershipRestriction) {
    this.displayName = displayName;
    this.urlSlug = urlSlug;
    this.scopeType = scopeType;
    this.isScoped = scopeType != null;
    this.allowedRoles = allowedRoles;
    this.createNewInstanceRoute = createNewInstanceRoute;
    this.userMembershipRestriction = userMembershipRestriction;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUrlSlug() {
    return urlSlug;
  }

  public boolean isScoped() {
    return isScoped;
  }

  public String getScopeType() {
    return scopeType;
  }

  public List<Role> getAllowedRoles() {
    return allowedRoles;
  }

  public Set<Role> getAllowedRolesAsSet() {
    return EnumSet.copyOf(allowedRoles);
  }

  public String getCreateNewInstanceRoute() {
    return createNewInstanceRoute.get();
  }

  public UserMembershipRestriction getUserMembershipRestriction() {
    return userMembershipRestriction;
  }

  public static Optional<TeamType> fromUrlSlug(String urlSlug) {
    return Arrays.stream(values())
        .filter(teamType -> teamType.urlSlug.equals(urlSlug))
        .findFirst();
  }

}
