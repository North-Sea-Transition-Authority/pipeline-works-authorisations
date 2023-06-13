package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import java.time.Instant;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadLocationDetailTestUtil {

  private PadLocationDetailTestUtil() {
    //no instantiation
  }

  public static PadLocationDetails createPadLocationDetails(PwaApplicationDetail pwaApplicationDetail) {

    var pl = new PadLocationDetails();
    pl.setPwaApplicationDetail(pwaApplicationDetail);
    pl.setWithinSafetyZone(HseSafetyZone.YES);
    pl.setPsrNotificationSubmittedOption(PsrNotification.YES);
    pl.setPsrNotificationSubmittedMonth(5);
    pl.setPsrNotificationSubmittedYear(2020);
    pl.setPsrNotificationExpectedSubmissionMonth(5);
    pl.setPsrNotificationExpectedSubmissionYear(20201);
    pl.setPsrNotificationNotRequiredReason("reason");
    pl.setDiversUsed(true);
    pl.setFacilitiesOffshore(true);
    pl.setPipelineAshoreLocation("ashore");
    pl.setTransportsMaterialsToShore(false);
    pl.setTransportationMethodToShore("transport");
    pl.setTransportsMaterialsFromShore(false);
    pl.setTransportationMethodFromShore("transport");
    pl.setRouteSurveyUndertaken(false);
    pl.setRouteSurveyNotUndertakenReason("reason");
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
