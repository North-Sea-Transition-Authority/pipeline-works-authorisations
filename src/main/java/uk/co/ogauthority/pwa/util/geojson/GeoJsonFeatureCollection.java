package uk.co.ogauthority.pwa.util.geojson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * See <a href="https://datatracker.ietf.org/doc/html/rfc7946#section-1.5">GeoJson reference</a>.
 */
@JsonIgnoreProperties({"FEATURE_COLLECTION_TYPE"})
public final class GeoJsonFeatureCollection {
  private static final String FEATURE_COLLECTION_TYPE = "FeatureCollection";

  private final String type;
  private final List<GeoJsonFeature> features;

  GeoJsonFeatureCollection(List<GeoJsonFeature> features) {
    this.type = FEATURE_COLLECTION_TYPE;
    this.features = features;
  }

  public String getType() {
    return type;
  }

  public List<GeoJsonFeature> getFeatures() {
    return features;
  }
}
