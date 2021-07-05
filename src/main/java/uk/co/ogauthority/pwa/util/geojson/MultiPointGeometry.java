package uk.co.ogauthority.pwa.util.geojson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See <a href="https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.3">GeoJson reference</a>.
 */
@JsonIgnoreProperties({ "MULTI_POINT_TYPE", "geometryPositions"})
public final class MultiPointGeometry extends GeoJsonGeometry {

  public static final String MULTI_POINT_TYPE = "MultiPoint";

  private final List<GeoJsonPosition> geometryPositions;
  private final List<Double[]> coordinates;

  MultiPointGeometry(List<GeoJsonPosition> geometryPositions) {
    super(MULTI_POINT_TYPE);
    this.geometryPositions = geometryPositions;
    this.coordinates = geometryPositions.stream()
        .map(GeoJsonPosition::getCoordinates)
        .collect(Collectors.toUnmodifiableList());

  }

  @Override
  public Object[] getCoordinates() {
    return this.coordinates.toArray();
  }

  public List<GeoJsonPosition> getGeometryPositions() {
    return geometryPositions;
  }


}
