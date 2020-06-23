package uk.co.ogauthority.pwa.model.enums.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees.ConsulteeGroupTeamManagementController;
import uk.co.ogauthority.pwa.controller.teams.PortalTeamManagementController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

/**
 * Enumeration of categories available inside 'Manage teams' screen.
 */
public enum ManageTeamType {

  REGULATOR_TEAM(
      "OGA team",
      "Team for regulator users",
      null, // requires res id so cannot be hardcoded
      10
  ),

  ORGANISATION_TEAMS(
      "Organisation group teams",
      "Teams for industry users",
      ReverseRouter.route(on(PortalTeamManagementController.class).renderManageableTeams(null)),
      20
  ),
  CONSULTEE_GROUP_TEAMS(
      "Consultee group teams",
      "Teams for consultee users",
      ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)),
      30
  );

  private final String linkText;
  private final String linkHint;
  private final String linkUrl;
  private final int displayOrder;

  ManageTeamType(String linkText, String linkHint, String linkUrl, int displayOrder) {
    this.linkText = linkText;
    this.linkHint = linkHint;
    this.linkUrl = linkUrl;
    this.displayOrder = displayOrder;
  }

  public String getLinkText() {
    return linkText;
  }

  public String getLinkHint() {
    return linkHint;
  }

  public String getLinkUrl() {
    return linkUrl;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
