package uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

public class AppProcessingTaskWarningTestUtil {


  public static NonBlockingTasksWarning createWithNoWarning() {
    return new NonBlockingTasksWarning(false, null, null);
  }

  public static NonBlockingTasksWarning createWithWarning(PwaApplication pwaApplication) {
    return new NonBlockingTasksWarning(
        true, "tasks incomplete",
        NonBlockingWarningReturnMessage.withoutSuffixMessage("message", "link", CaseManagementUtils.routeCaseManagement(pwaApplication)));
  }


}
