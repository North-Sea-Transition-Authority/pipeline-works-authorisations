package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.Checkable;

public class ContactTeamRoleView implements Checkable {

  private final String roleName;
  private final String title;
  private final String description;
  private final int displaySequence;

  public ContactTeamRoleView(String roleName, String title, String description, int displaySequence) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContactTeamRoleView that = (ContactTeamRoleView) o;
    return displaySequence == that.displaySequence && Objects.equals(roleName,
        that.roleName) && Objects.equals(title, that.title) && Objects.equals(description,
        that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleName, title, description, displaySequence);
  }

}
