package uk.co.ogauthority.pwa.features.datatypes.geojson;

/**
 * A Geometry object represents points, curves, and surfaces in coordinate space. Every Geometry object is a GeoJSON
 * object no matter where it occurs in a GeoJSON text.
 *
 * <p>See <a href="https://datatracker.ietf.org/doc/html/rfc7946#section-3.1">GeoJson reference</a></p>
 */
public abstract class GeoJsonGeometry {
  protected final String type;

  protected GeoJsonGeometry(String type) {
    this.type = type;
  }

  /**
   * Decimal degrees e.g [0.9, 41.1].
   */
  public abstract Object[] getCoordinates();

  public String getType() {
    return type;
  }
}
