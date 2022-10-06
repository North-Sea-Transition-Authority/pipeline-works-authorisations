package uk.co.ogauthority.pwa.features.email.teammangement;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class AddedToTeamEmailProps extends EmailProperties {
  private final String teamName;

  private final String rolesAssigned;

  public AddedToTeamEmailProps(String recipientFullName, String teamName, String rolesAssigned) {
    super(NotifyTemplate.ADDED_MEMBER_TO_TEAM, recipientFullName);
    this.teamName = teamName;
    this.rolesAssigned = rolesAssigned;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("TEAM_ADDED_TO", teamName);
    emailPersonalisation.put("ROLES_ASSIGNED", rolesAssigned);
    return emailPersonalisation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AddedToTeamEmailProps that = (AddedToTeamEmailProps) o;
    return Objects.equals(teamName, that.teamName)
        && Objects.equals(getRecipientFullName(), that.getRecipientFullName())
        && Objects.equals(rolesAssigned, that.rolesAssigned);
  }

  @Override
  public int hashCode() {
    return Objects.hash(teamName, getRecipientFullName(), rolesAssigned);
  }
}
