package uk.co.ogauthority.pwa.model.entity.asbuilt;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class AsBuiltNotificationWorkareaViewTestUtil {

  public static AsBuiltNotificationWorkareaView createAsBuiltNotificationViewFrom(Integer ngId,
                                                                                  Integer pwaId,
                                                                                  String projectName,
                                                                                  AsBuiltNotificationGroupStatus status) {
    var pwaRef = "1/W/" + ngId;
    var ngRef = "PA/" + pwaId;
    var consentId = 100;
    return new AsBuiltNotificationWorkareaView(
        ngId, ngRef, LocalDate.now(), Instant.now(), status, projectName, pwaId, pwaRef, consentId, List.of()
    );
  }

  public static AsBuiltNotificationWorkareaView createAsBuiltNotificationViewFrom(Integer ngId,
                                                                                  Integer pwaId,
                                                                                  String projectName,
                                                                                  AsBuiltNotificationGroupStatus status,
                                                                                  LocalDate deadlineDate) {
    var pwaRef = "1/W/" + ngId;
    var ngRef = "PA/" + pwaId;
    var consentId = 100;
    return new AsBuiltNotificationWorkareaView(
        ngId, ngRef, deadlineDate, Instant.now(), status, projectName, pwaId, pwaRef, consentId, List.of()
    );
  }

}