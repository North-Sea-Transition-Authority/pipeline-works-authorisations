package uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class PublicNoticeApprovalRequestEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String publicNoticeReason;
  private final String caseManagementLink;

  public PublicNoticeApprovalRequestEmailProps(String recipientFullName,
                                               String applicationReference,
                                               String publicNoticeReason,
                                               String caseManagementLink) {
    super(NotifyTemplate.PUBLIC_NOTICE_APPROVAL_REQUEST, recipientFullName);
    this.applicationReference = applicationReference;
    this.publicNoticeReason = publicNoticeReason;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("PUBLIC_NOTICE_REASON", publicNoticeReason);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
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
    PublicNoticeApprovalRequestEmailProps that = (PublicNoticeApprovalRequestEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(publicNoticeReason, that.publicNoticeReason)
        && Objects.equals(caseManagementLink, that.caseManagementLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, publicNoticeReason, caseManagementLink);
  }
}
