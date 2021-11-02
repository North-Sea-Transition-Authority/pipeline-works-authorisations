package uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class PublicNoticeUpdateRequestEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String documentComments;
  private final String caseManagementLink;

  public PublicNoticeUpdateRequestEmailProps(String recipientFullName,
                                             String applicationReference,
                                             String documentComments,
                                             String caseManagementLink) {
    super(NotifyTemplate.PUBLIC_NOTICE_UPDATE_REQUESTED, recipientFullName);
    this.applicationReference = applicationReference;
    this.documentComments = documentComments;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("DOCUMENT_COMMENTS", documentComments);
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
    PublicNoticeUpdateRequestEmailProps that = (PublicNoticeUpdateRequestEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(documentComments, that.documentComments)
        && Objects.equals(caseManagementLink, that.caseManagementLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, documentComments, caseManagementLink);
  }
}
