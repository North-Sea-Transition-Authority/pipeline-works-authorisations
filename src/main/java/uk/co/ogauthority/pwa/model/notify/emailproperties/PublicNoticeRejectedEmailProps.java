package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class PublicNoticeRejectedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String rejectionReason;
  private final String caseManagementLink;

  public PublicNoticeRejectedEmailProps(String recipientFullName,
                                        String applicationReference,
                                        String rejectionReason,
                                        String caseManagementLink) {
    super(NotifyTemplate.PUBLIC_NOTICE_REJECTED, recipientFullName);
    this.applicationReference = applicationReference;
    this.rejectionReason = rejectionReason;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("REJECTION_REASON", rejectionReason);
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
    PublicNoticeRejectedEmailProps that = (PublicNoticeRejectedEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(rejectionReason, that.rejectionReason)
        && Objects.equals(caseManagementLink, that.caseManagementLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, rejectionReason, caseManagementLink);
  }
}
