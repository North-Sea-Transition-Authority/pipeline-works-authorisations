package uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class PublicNoticePublicationUpdateEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String caseManagementLink;
  private final String publicationDate;

  public PublicNoticePublicationUpdateEmailProps(String recipientFullName,
                                                 String applicationReference,
                                                 String caseManagementLink,
                                                 String publicationDate) {
    super(NotifyTemplate.PUBLIC_NOTICE_PUBLICATION_UPDATE, recipientFullName);
    this.applicationReference = applicationReference;
    this.caseManagementLink = caseManagementLink;
    this.publicationDate = publicationDate;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("PUBLICATION_DATE", publicationDate);
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
    PublicNoticePublicationUpdateEmailProps that = (PublicNoticePublicationUpdateEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(publicationDate, that.publicationDate)
        && Objects.equals(caseManagementLink, that.caseManagementLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, publicationDate, caseManagementLink);
  }

}
