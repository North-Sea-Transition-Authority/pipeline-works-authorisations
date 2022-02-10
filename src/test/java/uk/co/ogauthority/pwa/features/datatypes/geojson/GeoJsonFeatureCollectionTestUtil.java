package uk.co.ogauthority.pwa.features.datatypes.geojson;

import java.util.List;

public final class GeoJsonFeatureCollectionTestUtil {

  private GeoJsonFeatureCollectionTestUtil(){
    throw new UnsupportedOperationException("Not util for you!");
  }

  public static GeoJsonFeatureCollection createCollectionFrom(GeoJsonFeature geoJsonFeature){
    return new GeoJsonFeatureCollection(List.of(geoJsonFeature));
  }

  public static GeoJsonFeatureCollection createCollectionFrom(List<GeoJsonFeature> geoJsonFeatures){
    return new GeoJsonFeatureCollection(geoJsonFeatures);
  }

}