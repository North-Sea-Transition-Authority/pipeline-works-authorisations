package uk.co.ogauthority.pwa.model.location;

import java.math.RoundingMode;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;

/**
 * Data class to store a pair of lat/long coordinates.
 */
public class CoordinatePair implements DiffableAsString {

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

  public String getDisplayString() {
    var formatString =  "%s ° %s ' %s \" %s";
    var latString = String.format(
        formatString,
        latitude.getDegrees(),
        latitude.getMinutes(),
        latitude.getSeconds().setScale(4, RoundingMode.HALF_UP).toPlainString(),
        latitude.getDirection().getDisplayTextShort()
    );

    var longString = String.format(
        formatString,
        longitude.getDegrees(),
        longitude.getMinutes(),
        longitude.getSeconds().setScale(4, RoundingMode.HALF_UP).toPlainString(),
        longitude.getDirection().getDisplayTextShort()
    );

    return latString + "\n" + longString;
  }

  @Override
  public String getDiffableString() {
    return this.getDisplayString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CoordinatePair that = (CoordinatePair) o;
    return Objects.equals(latitude, that.latitude)
      && Objects.equals(longitude, that.longitude);
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude);
  }
}
