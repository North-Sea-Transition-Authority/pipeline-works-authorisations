package uk.co.ogauthority.pwa.testutils;

import java.time.Instant;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatusHistory;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;

public class AsBuiltNotificationGroupStatusHistoryTestUtil {

  public static AsBuiltNotificationGroupStatusHistory createDefaultAsBuiltStatusHistory() {
    var person = PersonTestUtil.createDefaultPerson();
    var asBuiltGroup = AsBuiltNotificationGroupTestUtil.createDefaultGroupWithConsent();
    return new AsBuiltNotificationGroupStatusHistory(asBuiltGroup, AsBuiltNotificationGroupStatus.IN_PROGRESS, person.getId(),
        Instant.now());
  }

  public static AsBuiltNotificationGroupStatusHistory createAsBuiltStatusHistory_withNotificationGroup(
      AsBuiltNotificationGroup asBuiltNotificationGroup, AsBuiltNotificationGroupStatus status) {
    var person = PersonTestUtil.createDefaultPerson();
    return new AsBuiltNotificationGroupStatusHistory(asBuiltNotificationGroup, status, person.getId(), Instant.now());
  }

}
