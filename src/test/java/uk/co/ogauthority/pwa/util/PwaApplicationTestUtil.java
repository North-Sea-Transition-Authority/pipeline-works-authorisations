package uk.co.ogauthority.pwa.util;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class PwaApplicationTestUtil {

  private static PwaApplicationDetail createApplicationDetail(MasterPwa masterPwa, PwaApplicationType applicationType,
                                                              int appId, int appDetailId) {
    var masterApp = new PwaApplication();
    masterApp.setMasterPwa(masterPwa);
    masterApp.setApplicationType(applicationType);
    masterApp.setId(appId);

    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(masterApp);
    detail.setId(appDetailId);

    return detail;

  }

  public static PwaApplicationDetail createDefaultApplicationDetail(PwaApplicationType applicationType) {
    var masterPwa = new MasterPwa(Instant.now());
    masterPwa.setId(10);

   return createApplicationDetail(masterPwa, applicationType, 20, 30);

  }
}
