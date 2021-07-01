package uk.co.ogauthority.pwa.service.asbuilt.view;

import java.util.List;

public class AsBuiltSubmissionHistoryViewUtil {

  public static AsBuiltSubmissionHistoryView createDefaultAsBuiltSubmissionHistoryView() {
    var notificationView1 = AsBuiltNotificationViewUtil.createDefaultAsBuiltNotificationView();
    var notificationView2 = AsBuiltNotificationViewUtil.createDefaultAsBuiltNotificationView();
    return new AsBuiltSubmissionHistoryView(notificationView1, List.of(notificationView2));
  }

}
