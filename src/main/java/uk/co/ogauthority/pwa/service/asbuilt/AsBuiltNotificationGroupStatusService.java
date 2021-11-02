package uk.co.ogauthority.pwa.service.asbuilt;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
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

  /**
   * Call this to create a new as-built group status history.
   *
   * @param asBuiltNotificationGroup   - the as-built notification group object.
   * @param newGroupStatus    - the new status to set the as-built notification group to
   * @param person - the person who is creating the new status
   */
  void setGroupStatusIfNewOrChanged(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                    AsBuiltNotificationGroupStatus newGroupStatus,
                                    Person person) {
    var latestStatusHistoryOpt = getLatestStatusHistory(asBuiltNotificationGroup);

    var newStatusRequired = latestStatusHistoryOpt.isEmpty()
        || latestStatusHistoryOpt.filter(statusHistory -> statusHistory.getStatus() != newGroupStatus).isPresent();

    if (newStatusRequired) {
      checkNewAsBuiltStatusCanBeCreated(asBuiltNotificationGroup, latestStatusHistoryOpt, newGroupStatus);

      latestStatusHistoryOpt.ifPresent(statusHistory -> closeLatestStatusHistory(statusHistory, person));
      createNewAsBuiltGroupStatusHistory(asBuiltNotificationGroup, newGroupStatus, person);
    }

  }

  private void createNewAsBuiltGroupStatusHistory(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                                  AsBuiltNotificationGroupStatus status,
                                                  Person person) {
    var statusHistory = new AsBuiltNotificationGroupStatusHistory(asBuiltNotificationGroup, status,
        person.getId(), Instant.now());

    asBuiltNotificationGroupStatusHistoryRepository.save(statusHistory);
  }

  List<AsBuiltNotificationGroup> getAllNonCompleteAsBuiltNotificationGroups() {
    return asBuiltNotificationGroupStatusHistoryRepository
        .findAllByEndedTimestampIsNullAndStatusIsNot(AsBuiltNotificationGroupStatus.COMPLETE)
        .stream()
        .map(AsBuiltNotificationGroupStatusHistory::getAsBuiltNotificationGroup)
        .collect(Collectors.toList());
  }

  private void checkNewAsBuiltStatusCanBeCreated(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                                 Optional<AsBuiltNotificationGroupStatusHistory> latestStatusHistory,
                                                 AsBuiltNotificationGroupStatus newStatus) {
    if (newStatus != AsBuiltNotificationGroupStatus.NOT_STARTED) {
      latestStatusHistory.ifPresentOrElse(asBuiltNotificationGroupStatusHistory -> { }, () -> {
        throw new EntityNotFoundException(String.format("No prior status history found for as-built notification group with id %s",
                  asBuiltNotificationGroup.getId()));
      });
    }
  }

  private Optional<AsBuiltNotificationGroupStatusHistory> getLatestStatusHistory(AsBuiltNotificationGroup asBuiltNotificationGroup) {
    return asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(asBuiltNotificationGroup);
  }

  private void closeLatestStatusHistory(AsBuiltNotificationGroupStatusHistory asBuiltNotificationGroupStatusHistory,
                                        Person person) {
    asBuiltNotificationGroupStatusHistory.setEndedByPersonId(person.getId());
    asBuiltNotificationGroupStatusHistory.setEndedTimestamp(Instant.now());
    asBuiltNotificationGroupStatusHistoryRepository.save(asBuiltNotificationGroupStatusHistory);
  }

  boolean isGroupStatusComplete(AsBuiltNotificationGroup asBuiltNotificationGroup) {
    return asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndStatusAndEndedTimestampIsNull(
        asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE).isPresent();
  }

}
