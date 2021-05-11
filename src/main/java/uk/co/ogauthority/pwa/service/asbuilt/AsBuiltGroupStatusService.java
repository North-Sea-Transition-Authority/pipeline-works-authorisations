package uk.co.ogauthority.pwa.service.asbuilt;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatusHistory;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupStatusHistoryRepository;

/**
 * Perform database interactions for as--built notification group status changes.
 */
@Component
class AsBuiltGroupStatusService {

  private final AsBuiltNotificationGroupStatusHistoryRepository asBuiltNotificationGroupStatusHistoryRepository;

  private final Clock clock;

  @Autowired
  AsBuiltGroupStatusService(
      AsBuiltNotificationGroupStatusHistoryRepository asBuiltNotificationGroupStatusHistoryRepository,
      @Qualifier("utcClock") Clock clock) {
    this.asBuiltNotificationGroupStatusHistoryRepository = asBuiltNotificationGroupStatusHistoryRepository;
    this.clock = clock;
  }


  void setNewTipStatus(AsBuiltNotificationGroup asBuiltNotificationGroup,
                       AsBuiltNotificationGroupStatus status,
                       Person person) {

    var statusUpdateInstant = clock.instant();
    var personId = person.getId();

    asBuiltNotificationGroupStatusHistoryRepository
        .findByAsBuiltNotificationGroupAndEndedTimestampIsNull(asBuiltNotificationGroup)
        .ifPresent(asBuiltNotificationGroupStatusHistory -> {
          asBuiltNotificationGroupStatusHistory.setEndedTimestamp(statusUpdateInstant);
          asBuiltNotificationGroupStatusHistory.setEndedByPersonId(personId);
          asBuiltNotificationGroupStatusHistoryRepository.save(asBuiltNotificationGroupStatusHistory);
        });

    var newStatusHistory = new AsBuiltNotificationGroupStatusHistory(
        asBuiltNotificationGroup,
        status,
        personId,
        statusUpdateInstant
    );

    asBuiltNotificationGroupStatusHistoryRepository.save(newStatusHistory);
  }

}
