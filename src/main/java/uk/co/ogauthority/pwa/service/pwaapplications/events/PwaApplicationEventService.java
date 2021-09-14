package uk.co.ogauthority.pwa.service.pwaapplications.events;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.pwaapplications.events.PwaApplicationEventRepository;

@Component
public class PwaApplicationEventService {

  private final PwaApplicationEventRepository pwaApplicationEventRepository;
  private final Clock clock;

  @Autowired
  public PwaApplicationEventService(PwaApplicationEventRepository pwaApplicationEventRepository,
                                    @Qualifier("utcClock") Clock clock) {
    this.pwaApplicationEventRepository = pwaApplicationEventRepository;
    this.clock = clock;
  }

  @EventListener
  public void handleConsentIssueFailure(ConsentIssueFailedEvent consentIssueFailedEvent) {

    var failure = new PwaApplicationEvent(
        consentIssueFailedEvent.getPwaApplicationDetail().getPwaApplication(),
        PwaApplicationEventType.CONSENT_ISSUE_FAILED,
        clock.instant(),
        consentIssueFailedEvent.getIssuingUser());

    failure.setMessage(ExceptionUtils.getStackTrace(consentIssueFailedEvent.getException()));

    pwaApplicationEventRepository.save(failure);

  }

  public List<PwaApplicationEvent> getUnclearedEventsByApplicationAndType(PwaApplication pwaApplication,
                                                                          PwaApplicationEventType eventType) {

    return pwaApplicationEventRepository
        .findPwaApplicationEventsByPwaApplicationAndEventTypeAndEventClearedInstantIsNull(pwaApplication, eventType);

  }

  public void clearEvents(PwaApplication pwaApplication, PwaApplicationEventType eventType) {

    var clearedEvents = new ArrayList<PwaApplicationEvent>();

    getUnclearedEventsByApplicationAndType(pwaApplication, eventType).forEach(event -> {
      event.setEventClearedInstant(clock.instant());
      clearedEvents.add(event);
    });

    pwaApplicationEventRepository.saveAll(clearedEvents);

  }

}
