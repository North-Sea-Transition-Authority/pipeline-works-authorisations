package uk.co.ogauthority.pwa.model.entity.asbuilt;

import java.time.Instant;
import java.time.LocalDate;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

public class AsBuiltNotificationGroupDetailTestUtil {

  public static AsBuiltNotificationGroupDetail createAsBuiltNotificationGroupDetail_fromAsBuiltNotificationGroupAndDeadlineDateAndPerson(
      AsBuiltNotificationGroup asBuiltNotificationGroup, LocalDate deadlineDate, Person person) {
    return new AsBuiltNotificationGroupDetail(asBuiltNotificationGroup, deadlineDate, person.getId(), Instant.now());
  }
}
