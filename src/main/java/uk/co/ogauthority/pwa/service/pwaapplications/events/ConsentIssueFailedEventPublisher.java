package uk.co.ogauthority.pwa.service.pwaapplications.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Component
public class ConsentIssueFailedEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public ConsentIssueFailedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publishConsentIssueFailedEvent(PwaApplicationDetail pwaApplicationDetail,
                                             Exception exception,
                                             WebUserAccount issuingUser) {

    var event = new ConsentIssueFailedEvent(pwaApplicationDetail, exception, issuingUser);
    applicationEventPublisher.publishEvent(event);

  }

}
