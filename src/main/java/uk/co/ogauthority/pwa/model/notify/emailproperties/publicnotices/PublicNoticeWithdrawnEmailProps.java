package uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class PublicNoticeWithdrawnEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String withdrawingUserName;
  private final String withdrawalReason;

  public PublicNoticeWithdrawnEmailProps(String recipientFullName,
                                         String applicationReference,
                                         String withdrawingUserName,
                                         String withdrawalReason) {
    super(NotifyTemplate.PUBLIC_NOTICE_WITHDRAWN, recipientFullName);
    this.applicationReference = applicationReference;
    this.withdrawingUserName = withdrawingUserName;
    this.withdrawalReason = withdrawalReason;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("WITHDRAWING_USER_NAME", withdrawingUserName);
    emailPersonalisation.put("WITHDRAWAL_REASON", withdrawalReason);
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
    PublicNoticeWithdrawnEmailProps that = (PublicNoticeWithdrawnEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(withdrawingUserName, that.withdrawingUserName)
        && Objects.equals(withdrawalReason, that.withdrawalReason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, withdrawingUserName, withdrawalReason);
  }
}
