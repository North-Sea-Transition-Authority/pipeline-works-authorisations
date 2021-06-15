package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.locationdetails;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.locationdetails.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.enums.locationdetails.PsrNotification;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.LocationDetailsView;

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
