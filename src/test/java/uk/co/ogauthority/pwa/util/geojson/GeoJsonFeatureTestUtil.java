package uk.co.ogauthority.pwa.util.geojson;


public final class GeoJsonFeatureTestUtil {

  private GeoJsonFeatureTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }

  public static GeoJsonFeature getFakeFeature(){
    return new GeoJsonFeature(
        new GeoJsonGeometry("FAKE!") {
          @Override
          public Object[] getCoordinates() {
            return new Object[0];
          }
        }
    );
  }
}