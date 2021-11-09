package uk.co.ogauthority.pwa.features.datatypes.geojson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See <a href="https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.4">GeoJson reference</a>.
 */
@JsonIgnoreProperties({ "LINE_STRING_TYPE", "geometryPositions"})
public final class LineGeometry extends GeoJsonGeometry {

  public static final String LINE_STRING_TYPE = "LineString";

  private final List<GeoJsonPosition> geometryPositions;
  private final List<Double[]> coordinates;

  LineGeometry(List<GeoJsonPosition> geometryPositions) {
    super(LINE_STRING_TYPE);
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
