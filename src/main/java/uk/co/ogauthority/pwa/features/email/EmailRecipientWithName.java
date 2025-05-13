package uk.co.ogauthority.pwa.features.email;

import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

public record EmailRecipientWithName(String fullName, String emailAddress) implements EmailRecipient {

  public static EmailRecipientWithName from(Person person) {
    return new EmailRecipientWithName(person.getFullName(), person.getEmailAddress());
  }

  public static EmailRecipientWithName from(TeamMemberView member) {
    return new EmailRecipientWithName(member.getFullName(), member.email());
  }

  @Override
  public String getEmailAddress() {
    return emailAddress;
  }
}
