package uk.co.ogauthority.pwa.features.datatypes.geojson;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Before;
import org.junit.Test;

public class GeoJsonFeatureFactoryTest {

  private GeoJsonFeatureFactory factory;

  @Before
  public void setUp() throws Exception {
    factory = new GeoJsonFeatureFactory();
  }

  @Test
  public void createFeatureCollection_noFeatures() {

    var featureCollection = factory.createFeatureCollection(List.of());
    assertThat(featureCollection.getFeatures()).isEmpty();
    assertThat(featureCollection.getType()).isEqualTo("FeatureCollection");
  }

  @Test
  public void createFeatureCollection_withFeatures() {


    var f1 = new GeoJsonFeature(new LineGeometry(List.of()));
    var f2 = new GeoJsonFeature(new MultiPointGeometry(List.of()));

    var featureCollection = factory.createFeatureCollection(List.of(f1, f2));
    assertThat(featureCollection.getFeatures()).containsExactly(f1, f2);
    assertThat(featureCollection.getType()).isEqualTo("FeatureCollection");
  }

  @Test(expected = IllegalArgumentException.class)
  public void createSimpleLineFeature_invalidPoints() {
    var p1 = new DecimalDegreesPointTestImpl(null, 1.2);
    var p2 = new DecimalDegreesPointTestImpl(1.2, 1.2);

     factory.createSimpleLineFeature(p1, p2);
  }

  @Test
  public void createSimpleLineFeature_validPoints() {
    var p1 = new DecimalDegreesPointTestImpl(0.0, 1.2);
    var p2 = new DecimalDegreesPointTestImpl(1.7, 1.2);

    var feature = factory.createSimpleLineFeature(p1, p2);

    assertThat(feature.getGeometry()).satisfies(geoJsonGeometry -> {
      assertThat(geoJsonGeometry.getCoordinates()).containsExactly(
          new Object[]{0.0, 1.2},
          new Object[]{1.7, 1.2}
      );
      assertThat(geoJsonGeometry.getType()).isEqualTo("LineString");
    });

    assertThat(feature.getProperties()).isEmpty();
    assertThat(feature.getType()).isEqualTo("Feature");
  }

  @Test(expected = IllegalArgumentException.class)
  public void createMultiPointFeature_validPoints_invalidPoints() {
    var p1 = new DecimalDegreesPointTestImpl(1.0, 1.2);
    var p2 = new DecimalDegreesPointTestImpl(1.2, null);

    factory.createMultiPointFeature(p1, p2);
  }

  @Test
  public void createMultiPointFeature_validPoints() {
    var p1 = new DecimalDegreesPointTestImpl(1.0, 2.0);
    var p2 = new DecimalDegreesPointTestImpl(3.0, 4.0);
    var p3 = new DecimalDegreesPointTestImpl(5.0, 6.0);

    var feature = factory.createMultiPointFeature(p1, p2, p3);

    assertThat(feature.getGeometry()).satisfies(geoJsonGeometry -> {
      assertThat(geoJsonGeometry.getCoordinates()).containsExactly(
          new Object[]{1.0, 2.0},
          new Object[]{3.0, 4.0},
          new Object[]{5.0, 6.0}
      );
      assertThat(geoJsonGeometry.getType()).isEqualTo("MultiPoint");
    });

    assertThat(feature.getProperties()).isEmpty();
    assertThat(feature.getType()).isEqualTo("Feature");

  }

  private static class DecimalDegreesPointTestImpl implements DecimalDegreesPoint {

    private final Double longitude;
    private final Double latitude;

    public DecimalDegreesPointTestImpl(Double longitude, Double latitude) {
      this.longitude = longitude;
      this.latitude = latitude;
    }

    @Override
    public Double getLongitudeDecimalDegrees() {
      return longitude;
    }

    @Override
    public Double getLatitudeDecimalDegrees() {
      return latitude;
    }

    @Override
    public boolean longAndLatHaveValue() {
      return ObjectUtils.allNotNull(longitude, latitude);
    }
  }

}