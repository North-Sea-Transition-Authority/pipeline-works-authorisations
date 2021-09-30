package uk.co.ogauthority.pwa.model.teams;

/**
 * The PWA global team. This has no level of scoping.
 */
public class PwaGlobalTeam extends PwaTeam {
  public PwaGlobalTeam(int id, String name, String description) {
    super(id, name, description, PwaTeamType.GLOBAL);
  }
}
