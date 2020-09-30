package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadFastTrack;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadFastTrack_;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadFastTrackTestUtil {
  private PadFastTrackTestUtil() {
    //no instantiation
  }

  public static PadFastTrack createPadFastTrack(PwaApplicationDetail pwaApplicationDetail){
    var fastTrack = new PadFastTrack();
    fastTrack.setPwaApplicationDetail(pwaApplicationDetail);
    fastTrack.setAvoidEnvironmentalDisaster(true);
    fastTrack.setEnvironmentalDisasterReason("Env Reason");
    fastTrack.setSavingBarrels(true);
    fastTrack.setSavingBarrelsReason("Barrels Reason");
    fastTrack.setProjectPlanning(true);
    fastTrack.setProjectPlanningReason("Planning reason");
    fastTrack.setHasOtherReason(true);
    fastTrack.setOtherReason("Other reason");

    ObjectTestUtils.assertAllFieldsNotNull(fastTrack, PadFastTrack.class, Set.of(PadFastTrack_.ID));

    return fastTrack;
  }
}
