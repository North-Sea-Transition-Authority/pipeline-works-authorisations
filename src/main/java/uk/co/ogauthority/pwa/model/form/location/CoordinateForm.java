package uk.co.ogauthority.pwa.model.form.location;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

/**
 * Form class to capture lat/long coordinates.
 */
public class CoordinateForm {

  private Integer latitudeDegrees;
  private Integer latitudeMinutes;
  private BigDecimal latitudeSeconds;
  private LatitudeDirection latitudeDirection;

  private Integer longitudeDegrees;
  private Integer longitudeMinutes;
  private BigDecimal longitudeSeconds;
  private LongitudeDirection longitudeDirection;

  public CoordinateForm() {
  }

  public Integer getLatitudeDegrees() {
    return latitudeDegrees;
  }

  public void setLatitudeDegrees(Integer latitudeDegrees) {
    this.latitudeDegrees = latitudeDegrees;
  }

  public Integer getLatitudeMinutes() {
    return latitudeMinutes;
  }

  public void setLatitudeMinutes(Integer latitudeMinutes) {
    this.latitudeMinutes = latitudeMinutes;
  }

  public BigDecimal getLatitudeSeconds() {
    return latitudeSeconds;
  }

  public void setLatitudeSeconds(BigDecimal latitudeSeconds) {
    this.latitudeSeconds = latitudeSeconds;
  }

  public LatitudeDirection getLatitudeDirection() {
    return Optional.ofNullable(latitudeDirection).orElse(LatitudeDirection.NORTH);
  }

  public void setLatitudeDirection(LatitudeDirection latitudeDirection) {
    this.latitudeDirection = latitudeDirection;
  }

  public Integer getLongitudeDegrees() {
    return longitudeDegrees;
  }

  public void setLongitudeDegrees(Integer longitudeDegrees) {
    this.longitudeDegrees = longitudeDegrees;
  }

  public Integer getLongitudeMinutes() {
    return longitudeMinutes;
  }

  public void setLongitudeMinutes(Integer longitudeMinutes) {
    this.longitudeMinutes = longitudeMinutes;
  }

  public BigDecimal getLongitudeSeconds() {
    return longitudeSeconds;
  }

  public void setLongitudeSeconds(BigDecimal longitudeSeconds) {
    this.longitudeSeconds = longitudeSeconds;
  }

  public LongitudeDirection getLongitudeDirection() {
    return longitudeDirection;
  }

  public void setLongitudeDirection(LongitudeDirection longitudeDirection) {
    this.longitudeDirection = longitudeDirection;
  }

  public boolean areAllFieldsNotNull() {
    return latitudeDegrees != null
        && latitudeMinutes != null
        && latitudeSeconds != null
        && longitudeDegrees != null
        && longitudeMinutes != null
        && longitudeSeconds != null
        && longitudeDirection != null;
  }


  public boolean compareFormLatitude(CoordinateForm coordinateForm) {
    if (Objects.equals(this, coordinateForm)) {
      return true;
    }
    if (coordinateForm == null || getClass() != coordinateForm.getClass()) {
      return false;
    }
    return Objects.equals(latitudeDegrees, coordinateForm.latitudeDegrees)
        && Objects.equals(latitudeMinutes, coordinateForm.latitudeMinutes)
        && Objects.equals(latitudeSeconds, coordinateForm.latitudeSeconds)
        && latitudeDirection == coordinateForm.latitudeDirection;
  }

  public boolean compareFormLongitude(CoordinateForm coordinateForm) {
    if (Objects.equals(this, coordinateForm)) {
      return true;
    }
    if (coordinateForm == null || getClass() != coordinateForm.getClass()) {
      return false;
    }
    return Objects.equals(longitudeDegrees, coordinateForm.longitudeDegrees)
        && Objects.equals(longitudeMinutes, coordinateForm.longitudeMinutes)
        && Objects.equals(longitudeSeconds, coordinateForm.longitudeSeconds)
        && longitudeDirection == coordinateForm.longitudeDirection;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CoordinateForm that = (CoordinateForm) o;
    return Objects.equals(latitudeDegrees, that.latitudeDegrees)
        && Objects.equals(latitudeMinutes, that.latitudeMinutes)
        && Objects.equals(latitudeSeconds, that.latitudeSeconds)
        && latitudeDirection == that.latitudeDirection
        && Objects.equals(longitudeDegrees, that.longitudeDegrees)
        && Objects.equals(longitudeMinutes, that.longitudeMinutes)
        && Objects.equals(longitudeSeconds, that.longitudeSeconds)
        && longitudeDirection == that.longitudeDirection;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitudeDegrees, latitudeMinutes, latitudeSeconds, latitudeDirection,
        longitudeDegrees, longitudeMinutes, longitudeSeconds, longitudeDirection);
  }

}



