package uk.co.ogauthority.pwa.service.asbuilt;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupDetailRepository;

/**
 * Perform database interactions for as--built notification group deadline changes.
 */
@Component
class AsBuiltGroupDeadlineService {

  private final AsBuiltNotificationGroupDetailRepository asBuiltNotificationGroupDetailRepository;
  private final Clock clock;

  @Autowired
  AsBuiltGroupDeadlineService(
      AsBuiltNotificationGroupDetailRepository asBuiltNotificationGroupDetailRepository,
      @Qualifier("utcClock") Clock clock) {
    this.asBuiltNotificationGroupDetailRepository = asBuiltNotificationGroupDetailRepository;
    this.clock = clock;
  }


  void setNewDeadline(AsBuiltNotificationGroup group, LocalDate deadline, Person person) {

    var personId = person.getId();
    var instant = clock.instant();
    asBuiltNotificationGroupDetailRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(group)
        .ifPresent(asBuiltNotificationGroupDetail -> {
          asBuiltNotificationGroupDetail.setEndedByPersonId(personId);
          asBuiltNotificationGroupDetail.setEndedTimestamp(instant);

          asBuiltNotificationGroupDetailRepository.save(asBuiltNotificationGroupDetail);
        });

    var newGroupDetail = new AsBuiltNotificationGroupDetail(group, deadline, personId, instant);

    asBuiltNotificationGroupDetailRepository.save(newGroupDetail);
  }

}
