package uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class PublicNoticePublicationEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String caseManagementLink;
  private final String publicationDate;
  private final String serviceName;

  public PublicNoticePublicationEmailProps(String recipientFullName,
                                           String applicationReference,
                                           String caseManagementLink,
                                           String publicationDate,
                                           String serviceName) {
    super(NotifyTemplate.PUBLIC_NOTICE_PUBLICATION, recipientFullName);
    this.applicationReference = applicationReference;
    this.caseManagementLink = caseManagementLink;
    this.publicationDate = publicationDate;
    this.serviceName = serviceName;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("PUBLICATION_DATE", publicationDate);
    emailPersonalisation.put("SERVICE_NAME", serviceName);
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
    PublicNoticePublicationEmailProps that = (PublicNoticePublicationEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(publicationDate, that.publicationDate)
        && Objects.equals(serviceName, that.serviceName)
        && Objects.equals(caseManagementLink, that.caseManagementLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, publicationDate, serviceName, caseManagementLink);
  }

}
