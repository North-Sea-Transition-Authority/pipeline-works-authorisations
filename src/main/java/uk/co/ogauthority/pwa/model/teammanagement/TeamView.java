package uk.co.ogauthority.pwa.model.teammanagement;

import uk.co.ogauthority.pwa.model.teams.PwaTeam;

public class TeamView {
  private final int id;
  private final String name;
  private final String description;
  private final String selectRoute;

  public TeamView(PwaTeam team, String selectRoute) {
    this.name = team.getName();
    this.id = team.getId();
    this.description = team.getDescription();
    this.selectRoute = selectRoute;
  }

  public String getSelectRoute() {
    return selectRoute;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getId() {
    return id;
  }
}
