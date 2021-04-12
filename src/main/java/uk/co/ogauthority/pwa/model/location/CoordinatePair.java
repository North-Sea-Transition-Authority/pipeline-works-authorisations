package uk.co.ogauthority.pwa.model.location;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
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
    var latString = latitude != null ? latitude.getDisplayString() : "";
    var longString = longitude != null ? longitude.getDisplayString() : "";

    return StringUtils.defaultString(latString, "") +
        "\n" +
        StringUtils.defaultString(longString, "");
  }

  public boolean hasValue() {
    return latitude.hasValue() && longitude.hasValue();
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
