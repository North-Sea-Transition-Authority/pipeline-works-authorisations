package uk.co.ogauthority.pwa.service.asbuilt;

import java.time.Instant;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatusHistory;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupStatusHistoryRepository;

@Service
class AsBuiltNotificationGroupStatusService {

  private final AsBuiltNotificationGroupStatusHistoryRepository asBuiltNotificationGroupStatusHistoryRepository;

  @Autowired
  AsBuiltNotificationGroupStatusService(
      AsBuiltNotificationGroupStatusHistoryRepository asBuiltNotificationGroupStatusHistoryRepository) {
    this.asBuiltNotificationGroupStatusHistoryRepository = asBuiltNotificationGroupStatusHistoryRepository;
  }

  void setInitialGroupStatus(AsBuiltNotificationGroup asBuiltNotificationGroup,
                       Person person) {
    var statusUpdateInstant = Instant.now();
    var personId = person.getId();
    var newStatusHistory = new AsBuiltNotificationGroupStatusHistory(
        asBuiltNotificationGroup,
        AsBuiltNotificationGroupStatus.NOT_STARTED,
        personId,
        statusUpdateInstant
    );

    asBuiltNotificationGroupStatusHistoryRepository.save(newStatusHistory);
  }

  void setGroupStatus(AsBuiltNotificationGroup asBuiltNotificationGroup,
                      AsBuiltNotificationGroupStatus asBuiltNotificationGroupStatus,
                      Person person) {
    var latestStatusHistory = getLatestStatusHistory(asBuiltNotificationGroup);
    if (!isGroupStatusStillInProgress(latestStatusHistory, asBuiltNotificationGroupStatus)) {
      closeLatestStatusHistory(latestStatusHistory, person);

      var statusHistory = new AsBuiltNotificationGroupStatusHistory(asBuiltNotificationGroup, asBuiltNotificationGroupStatus,
          person.getId(), Instant.now());
      if (asBuiltNotificationGroupStatus == AsBuiltNotificationGroupStatus.COMPLETE) {
        statusHistory.setEndedByPersonId(person.getId());
        statusHistory.setEndedTimestamp(Instant.now());
      }
      asBuiltNotificationGroupStatusHistoryRepository.save(statusHistory);
    }
  }

  private boolean isGroupStatusStillInProgress(AsBuiltNotificationGroupStatusHistory currentStatusHistory,
                                               AsBuiltNotificationGroupStatus status) {
    return currentStatusHistory.getStatus() == AsBuiltNotificationGroupStatus.IN_PROGRESS
        && currentStatusHistory.getStatus() == status;
  }

  private AsBuiltNotificationGroupStatusHistory getLatestStatusHistory(AsBuiltNotificationGroup asBuiltNotificationGroup) {
    return asBuiltNotificationGroupStatusHistoryRepository
        .findByAsBuiltNotificationGroupAndEndedTimestampIsNull(asBuiltNotificationGroup)
        .orElseThrow(
            () -> new EntityNotFoundException(
                String.format("No prior status history found for as-built notification group with id %s",
                    asBuiltNotificationGroup.getId())));
  }

  private void closeLatestStatusHistory(AsBuiltNotificationGroupStatusHistory asBuiltNotificationGroupStatusHistory,
                                        Person person) {
    asBuiltNotificationGroupStatusHistory.setEndedByPersonId(person.getId());
    asBuiltNotificationGroupStatusHistory.setEndedTimestamp(Instant.now());
    asBuiltNotificationGroupStatusHistoryRepository.save(asBuiltNotificationGroupStatusHistory);
  }

}
