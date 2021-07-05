package uk.co.ogauthority.pwa.util.geojson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Helper service to construct GeoJson serializable objects.
 */
@Service
public final class GeoJsonFeatureFactory {

  public GeoJsonFeatureCollection createFeatureCollection(List<GeoJsonFeature> geoJsonFeatures) {

    return new GeoJsonFeatureCollection(geoJsonFeatures);

  }

  public GeoJsonFeature createSimpleLineFeature(DecimalDegreesPoint firstPoint, DecimalDegreesPoint secondPoint) {
    if (!(validateDecimalDegreesPoint(firstPoint) && validateDecimalDegreesPoint(secondPoint))) {
      throw new IllegalArgumentException("Found at least one invalid DecimalDegreesPoint!");
    }

    var linePoints = List.of(firstPoint, secondPoint);
    var linePositions = createPositionListFrom(linePoints);

    return new GeoJsonFeature(
        new LineGeometry(linePositions)
    );
  }

  public GeoJsonFeature createMultiPointFeature(DecimalDegreesPoint firstPoint,
                                                DecimalDegreesPoint... otherPoints) {

    if (!(validateDecimalDegreesPoint(firstPoint)
        && Arrays.stream(otherPoints).allMatch(this::validateDecimalDegreesPoint))) {
      throw new IllegalArgumentException("Found at least one invalid DecimalDegreesPoint!");
    }

    var pointsList = new ArrayList<DecimalDegreesPoint>();
    pointsList.add(firstPoint);
    pointsList.addAll(Arrays.asList(otherPoints));

    var positionList = createPositionListFrom(pointsList);

    return new GeoJsonFeature(
        new MultiPointGeometry(positionList)
    );
  }

  private boolean validateDecimalDegreesPoint(DecimalDegreesPoint point) {
    return point.longAndLatHaveValue();
  }

  private List<GeoJsonPosition> createPositionListFrom(List<DecimalDegreesPoint> points) {

    return points.stream()
        .map(p -> new GeoJsonPosition(p.getLongitudeDecimalDegrees(), p.getLatitudeDecimalDegrees()))
        .collect(Collectors.toList());
  }

}
