package uk.co.ogauthority.pwa.model.teams;

/**
 * The OGA regulator team. This has no level of scoping.
 */
public class PwaRegulatorTeam extends PwaTeam {
  public PwaRegulatorTeam(int id, String name, String description) {
    super(id, name, description, PwaTeamType.REGULATOR);
  }
}
