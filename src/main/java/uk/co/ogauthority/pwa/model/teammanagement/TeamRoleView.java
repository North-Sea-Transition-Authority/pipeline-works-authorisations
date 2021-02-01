package uk.co.ogauthority.pwa.model.teammanagement;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.Checkable;
import uk.co.ogauthority.pwa.model.teams.PwaRole;

public class TeamRoleView implements Checkable {

  private final String roleName;
  private final String title;
  private final String description;
  private final int displaySequence;

  public TeamRoleView(String roleName, String title, String description, int displaySequence) {
    this.roleName = roleName;
    this.title = title;
    this.description = description;
    this.displaySequence = displaySequence;
  }

  public String getTitle() {
    return title;
  }

  public int getDisplaySequence() {
    return displaySequence;
  }

  public String getRoleName() {
    return roleName;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String getIdentifier() {
    return this.roleName;
  }

  @Override
  public String getDisplayName() {
    return this.description;
  }

  public static TeamRoleView createTeamRoleViewFrom(PwaRole pwaRole) {
    return new TeamRoleView(
        pwaRole.getName(),
        pwaRole.getTitle(),
        pwaRole.getDescription(),
        pwaRole.getDisplaySequence());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TeamRoleView that = (TeamRoleView) o;
    return displaySequence == that.displaySequence && Objects.equals(roleName,
        that.roleName) && Objects.equals(title, that.title) && Objects.equals(description,
        that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleName, title, description, displaySequence);
  }

}
