package uk.co.ogauthority.pwa.model.notify.emailproperties.asbuilt;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class AsBuiltNotificationDeadlineUpcomingEmailProps extends EmailProperties {

  private final String asBuiltGroupReferences;
  private final String asBuiltWorkareaLink;

  public AsBuiltNotificationDeadlineUpcomingEmailProps(String recipientFullName,
                                                     String asBuiltGroupReferences, String asBuiltWorkareaLink) {
    super(NotifyTemplate.AS_BUILT_DEADLINE_UPCOMING, recipientFullName);
    this.asBuiltGroupReferences = asBuiltGroupReferences;
    this.asBuiltWorkareaLink = asBuiltWorkareaLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("AS_BUILT_GROUP_REFERENCES", asBuiltGroupReferences);
    emailPersonalisation.put("AS_BUILT_WORKAREA_LINK", asBuiltWorkareaLink);
    return emailPersonalisation;
  }

}
