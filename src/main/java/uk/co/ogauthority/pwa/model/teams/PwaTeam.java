package uk.co.ogauthority.pwa.model.teams;

public abstract class PwaTeam {

  private final int id;

  private final String name;

  private final String description;

  private final PwaTeamType type;

  PwaTeam(int id, String name, String description, PwaTeamType type) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.type = type;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public PwaTeamType getType() {
    return type;
  }
}
