package uk.co.ogauthority.pwa.model.teammanagement;

import java.util.Set;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;

/**
 * View of single team member for team management screen.
 */
public class TeamMemberView {

  private final String forename;
  private final String surname;
  private final String emailAddress;

  private final String editRoute;
  private final String removeRoute;
  private final Set<TeamRoleView> roleViews;

  public TeamMemberView(Person person, String editRoute, String removeRoute, Set<TeamRoleView> teamRoleViews) {
    this.forename = person.getForename();
    this.surname = person.getSurname();
    this.emailAddress = person.getEmailAddress();
    this.roleViews = teamRoleViews;
    this.editRoute = editRoute;
    this.removeRoute = removeRoute;
  }

  public String getEditRoute() {
    return editRoute;
  }

  public String getRemoveRoute() {
    return removeRoute;
  }

  public String getForename() {
    return forename;
  }

  public String getSurname() {
    return surname;
  }

  public String getFullName() {
    return this.forename + " " + this.surname;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public Set<TeamRoleView> getRoleViews() {
    return roleViews;
  }
}
