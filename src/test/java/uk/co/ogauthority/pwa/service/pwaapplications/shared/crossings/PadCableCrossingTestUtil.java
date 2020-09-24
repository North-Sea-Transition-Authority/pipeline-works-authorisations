package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossing_;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadCableCrossingTestUtil {

  private PadCableCrossingTestUtil() {
    // no instantiation
  }

  public static PadCableCrossing createPadCableCrossing(PwaApplicationDetail pwaApplicationDetail){
    var padCableCrossing = new PadCableCrossing();
    padCableCrossing.setCableName("cableName");
    padCableCrossing.setCableOwner("cableOwner");
    padCableCrossing.setLocation("locationDetails");
    padCableCrossing.setPwaApplicationDetail(pwaApplicationDetail);

    ObjectTestUtils.assertAllFieldsNotNull(
        padCableCrossing,
        PadCableCrossing.class,
        Set.of(PadCableCrossing_.ID)
    );

    return padCableCrossing;
  }
}
