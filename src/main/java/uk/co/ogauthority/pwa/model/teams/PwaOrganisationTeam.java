package uk.co.ogauthority.pwa.model.teams;

import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;

/**
 * A PwaTeam scoped to a PortalOrganisationGroup.
 */
public class PwaOrganisationTeam extends PwaTeam {

  private final PortalOrganisationGroup portalOrganisationGroup;

  public PwaOrganisationTeam(int id, String name, String description, PortalOrganisationGroup portalOrganisationGroup) {
    super(id, name, description, PwaTeamType.ORGANISATION);
    this.portalOrganisationGroup = portalOrganisationGroup;
  }

  public PortalOrganisationGroup getPortalOrganisationGroup() {
    return portalOrganisationGroup;
  }

}
