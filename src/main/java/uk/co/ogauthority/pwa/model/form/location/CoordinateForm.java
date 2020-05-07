package uk.co.ogauthority.pwa.model.form.location;

import java.math.BigDecimal;
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
}
