package uk.co.ogauthority.pwa.service.pwaapplications.shared.location;

import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class LocationDetailsUrlFactory {

  private final PwaApplicationType pwaApplicationType;
  private final Integer applicationId;

  public LocationDetailsUrlFactory(
      PwaApplicationType pwaApplicationType, Integer applicationId) {
    this.pwaApplicationType = pwaApplicationType;
    this.applicationId = applicationId;
  }

}
