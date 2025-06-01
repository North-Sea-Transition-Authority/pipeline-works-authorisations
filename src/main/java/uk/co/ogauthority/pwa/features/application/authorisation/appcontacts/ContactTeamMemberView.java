package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

/**
 * View of single team member for team management screen.
 */
public class ContactTeamMemberView {

  private final String forename;
  private final String surname;
  private final String emailAddress;
  private final String telephoneNo;

  private final String editRoute;
  private final String removeRoute;
  private final Set<ContactTeamRoleView> roleViews;

  public ContactTeamMemberView(Person person, String editRoute, String removeRoute, Set<ContactTeamRoleView> contactTeamRoleViews) {
    this.forename = person.getForename();
    this.surname = person.getSurname();
    this.emailAddress = person.getEmailAddress();
    this.telephoneNo = person.getTelephoneNo();
    this.roleViews = contactTeamRoleViews;
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

  public String getTelephoneNo() {
    return telephoneNo;
  }

  public Set<ContactTeamRoleView> getRoleViews() {
    return roleViews;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContactTeamMemberView that = (ContactTeamMemberView) o;
    return Objects.equals(forename, that.forename) && Objects.equals(surname,
        that.surname) && Objects.equals(emailAddress, that.emailAddress) && Objects.equals(
        telephoneNo, that.telephoneNo) && Objects.equals(editRoute, that.editRoute) && Objects.equals(
        removeRoute, that.removeRoute) && Objects.equals(roleViews, that.roleViews);
  }

  @Override
  public int hashCode() {
    return Objects.hash(forename, surname, emailAddress, telephoneNo, editRoute, removeRoute, roleViews);
  }

}
