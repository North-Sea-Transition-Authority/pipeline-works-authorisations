package uk.co.ogauthority.pwa.testutils;

import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltNotificationGroupSummaryView;

public class AsBuiltNotificationSummaryTestUtil {

  public static AsBuiltNotificationGroupSummaryView getAsBuiltNotificationSummmary() {
    return new AsBuiltNotificationGroupSummaryView(
      "app type",
      "CONSENT_REF",
      "APP_REF",
      "HOLDER",
      "10/10/2030",
      "access link"
    );
  }

}
