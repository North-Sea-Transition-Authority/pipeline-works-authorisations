package uk.co.ogauthority.pwa.model.location;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.util.geojson.DecimalDegreesPoint;

/**
 * Data class to store a pair of lat/long coordinates.
 */
public class CoordinatePair implements DiffableAsString, DecimalDegreesPoint {

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
  public boolean longAndLatHaveValue() {
    return hasValue();
  }

  @Override
  public Double getLongitudeDecimalDegrees() {
    var value = longitude.convertToDecimalDegrees();
    if (longitude.getDirection() == LongitudeDirection.WEST) {
      return value * -1;
    }
    return value;
  }

  @Override
  public Double getLatitudeDecimalDegrees() {
    var value = latitude.convertToDecimalDegrees();
    if (latitude.getDirection() == LatitudeDirection.SOUTH) {
      return value * -1;
    }
    return value;
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
