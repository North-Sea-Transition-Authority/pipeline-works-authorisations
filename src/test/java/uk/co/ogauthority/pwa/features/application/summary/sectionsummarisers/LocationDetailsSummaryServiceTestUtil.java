package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import java.util.List;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.HseSafetyZone;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.LocationDetailsView;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PsrNotification;

public class LocationDetailsSummaryServiceTestUtil {


  // no instantiation
  private LocationDetailsSummaryServiceTestUtil() {
  }

  public static LocationDetailsView createLocationDetailsView() {

    return new LocationDetailsView(
        "50m",
        HseSafetyZone.YES,
        PsrNotification.YES,
        "March 2020",
        null,
        true,
        List.of(),
        List.of(),
        false,
        false,
        "method",
        "details",
        true,
        null,
        true,
        "March 2020",
        "location",
        List.of());

  }


}
