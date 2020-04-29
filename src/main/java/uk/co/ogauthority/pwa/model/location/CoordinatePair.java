package uk.co.ogauthority.pwa.model.location;

/**
 * Data class to store a pair of lat/long coordinates.
 */
public class CoordinatePair {

  private LatitudeCoordinate latitude;

  private LongitudeCoordinate longitude;

  public CoordinatePair(LatitudeCoordinate latitude, LongitudeCoordinate longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public LatitudeCoordinate getLatitude() {
    return latitude;
  }

  public void setLatitude(LatitudeCoordinate latitude) {
    this.latitude = latitude;
  }

  public LongitudeCoordinate getLongitude() {
    return longitude;
  }

  public void setLongitude(LongitudeCoordinate longitude) {
    this.longitude = longitude;
  }
}
