package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.locationdetails;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.LocationDetailsView;

public class LocationDetailsSummaryServiceTestUtil {


  // no instantiation
  private LocationDetailsSummaryServiceTestUtil() {
  }

  public static LocationDetailsView createLocationDetailsView() {

    return new LocationDetailsView(
        "50m",
        HseSafetyZone.YES,
        true,
        "March 2020",
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
