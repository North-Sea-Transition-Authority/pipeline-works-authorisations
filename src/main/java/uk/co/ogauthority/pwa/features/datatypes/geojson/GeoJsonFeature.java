package uk.co.ogauthority.pwa.features.datatypes.geojson;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GeoJsonFeature {

  private final String type;
  private final Map<String, String> properties;
  private final GeoJsonGeometry geometry;

  GeoJsonFeature(GeoJsonGeometry geometry) {
    this.type = "Feature";
    // linked hash map to maintain insertion order
    this.properties = new LinkedHashMap<>();
    this.geometry = geometry;
  }

  public String getType() {
    return type;
  }

  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(properties);
  }

  public GeoJsonGeometry getGeometry() {
    return geometry;
  }

  public void addProperty(String key, String value) {
    this.properties.put(key, value);
  }
}
