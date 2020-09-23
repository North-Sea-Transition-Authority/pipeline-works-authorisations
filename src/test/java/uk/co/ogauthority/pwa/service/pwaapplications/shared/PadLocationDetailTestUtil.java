package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import java.time.Instant;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails_;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadLocationDetailTestUtil {

  private PadLocationDetailTestUtil() {
    //no instantiation
  }

  public static PadLocationDetails createPadLocationDetails(PwaApplicationDetail pwaApplicationDetail) {

    var pl = new PadLocationDetails();
    pl.setPwaApplicationDetail(pwaApplicationDetail);
    pl.setWithinSafetyZone(HseSafetyZone.YES);
    pl.setFacilitiesOffshore(true);
    pl.setPipelineAshoreLocation("ashore");
    pl.setTransportsMaterialsToShore(false);
    pl.setTransportationMethod("transport");
    pl.setRouteSurveyUndertaken(false);
    pl.setSurveyConcludedTimestamp(Instant.now());
    pl.setApproximateProjectLocationFromShore("approx location");
    pl.setPipelineRouteDetails("pipeline route details");
    pl.setWithinLimitsOfDeviation(false);

    ObjectTestUtils.assertAllFieldsNotNull(
        pl,
        PadLocationDetails.class,
        Set.of(PadLocationDetails_.ID)
    );
    return pl;

  }
}
