package uk.co.ogauthority.pwa.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.management.ScopedTeamManagementController;

public enum TeamType {

  REGULATOR(
      "Regulator",
      "regulator",
      false,
      List.of(
          Role.PWA_ACCESS,
          Role.TEAM_ADMINISTRATOR,
          Role.ORGANISATION_MANAGER,
          Role.PWA_MANAGER,
          Role.CASE_OFFICER,
          Role.CONSENT_VIEWER,
          Role.AS_BUILT_NOTIFICATION_ADMIN,
          Role.TEMPLATE_CLAUSE_MANAGER
      ),
      null
  ),
  CONSULTEE(
      "Consultees",
      "consultee",
      false,
      List.of(
          Role.PWA_ACCESS,
          Role.TEAM_ADMINISTRATOR,
          Role.RECIPIENT,
          Role.RESPONDER
      ),
      null
  ),
  ORGANISATION(
      "Organisations",
      "organisation",
      true,
      List.of(
          Role.PWA_ACCESS,
          Role.TEAM_ADMINISTRATOR,
          Role.APPLICATION_CREATOR,
          Role.APPLICATION_SUBMITTER,
          Role.FINANCE_ADMIN,
          Role.AS_BUILT_NOTIFICATION_SUBMITTER
      ),
          () -> ReverseRouter.route(on(ScopedTeamManagementController.class).renderCreateNewOrgTeam(null))
  );

  private final String displayName;
  private final String urlSlug;
  private final boolean isScoped;
  private final List<Role> allowedRoles;
  private final Supplier<String> createNewInstanceRoute;

  TeamType(String displayName, String urlSlug, boolean isScoped, List<Role> allowedRoles, Supplier<String> createNewInstanceRoute) {
    this.displayName = displayName;
    this.urlSlug = urlSlug;
    this.isScoped = isScoped;
    this.allowedRoles = allowedRoles;
    this.createNewInstanceRoute = createNewInstanceRoute;
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

  public List<Role> getAllowedRoles() {
    return allowedRoles;
  }

  public String getCreateNewInstanceRoute() {
    return createNewInstanceRoute.get();
  }

  public static Optional<TeamType> fromUrlSlug(String urlSlug) {
    return Arrays.stream(values())
        .filter(teamType -> teamType.urlSlug.equals(urlSlug))
        .findFirst();
  }

}
