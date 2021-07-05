package uk.co.ogauthority.pwa.util.geojson;

/**
 * <p>A position is an array of numbers.  There MUST be two or more elements.
 * The first two elements are longitude and latitude precisely in that order and using decimal numbers.</p>
 * <p>GeoJson spec reference https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1.</p>
 */
public final class GeoJsonPosition {
  private final Double[] coordinates;

  public GeoJsonPosition(double decimalDegreesLongitude, double decimalDegreesLatitude) {
    this.coordinates = new Double[]{decimalDegreesLongitude, decimalDegreesLatitude};
  }

  public Double[] getCoordinates() {
    return coordinates;
  }
}
