package uk.co.ogauthority.pwa.testutils;

import java.time.Instant;
import java.util.function.Consumer;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class PwaApplicationTestUtil {

  public static PwaApplicationDetail createApplicationDetail(MasterPwa masterPwa,
                                                             PwaApplicationType applicationType,
                                                             PwaApplicationStatus pwaApplicationStatus,
                                                              int appId,
                                                             int appDetailId) {
    var masterApp = new PwaApplication();
    masterApp.setMasterPwa(masterPwa);
    masterApp.setApplicationType(applicationType);
    masterApp.setId(appId);
    masterApp.setAppReference("APP_REFERENCE/" + appId);

    var detail = new PwaApplicationDetail();
    detail.setPwaApplication(masterApp);
    detail.setId(appDetailId);
    detail.setStatus(pwaApplicationStatus);
    detail.setTipFlag(true);
    detail.setVersionNo(1);
    detail.setSubmittedAsFastTrackFlag(false);

    return detail;

  }

  public static PwaApplicationDetail createDefaultApplicationDetail(PwaApplicationType applicationType) {
    return createDefaultApplicationDetail(
        PwaApplicationType.INITIAL
        , 20
    );
  }

  public static PwaApplicationDetail createDefaultApplicationDetail(PwaApplicationType applicationType, int appId) {
    var masterPwa = new MasterPwa(Instant.now());
    masterPwa.setId(10);

    return createApplicationDetail(masterPwa, applicationType, PwaApplicationStatus.DRAFT, appId, 30);

  }

  public static void tryAssertionWithStatus(PwaApplicationStatus status, Consumer<PwaApplicationStatus> tryBlock){
    try{
      tryBlock.accept(status);
    } catch(AssertionError e){
      throw new AssertionError("Failed assertion with status:" + status + "\n" + e.getMessage(), e);
    }
  }

  public static void tryAssertionWithPwaContactRole(PwaContactRole role, Consumer<PwaContactRole> tryBlock){
    try{
      tryBlock.accept(role);
    } catch(AssertionError e){
      throw new AssertionError("Failed assertion with role:" + role + "\n" + e.getMessage(), e);
    }
  }


}
