package uk.co.ogauthority.pwa.features.application.tasks.pipelines.geojsonview;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineFlexibility;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.geojson.GeoJsonFeature;
import uk.co.ogauthority.pwa.util.geojson.GeoJsonFeatureCollectionTestUtil;
import uk.co.ogauthority.pwa.util.geojson.GeoJsonFeatureFactory;
import uk.co.ogauthority.pwa.util.geojson.GeoJsonFeatureTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPipelineGeoJsonViewFactoryTest {

  @Mock
  private PadPipelineService pipelineService;

  @Mock
  private GeoJsonFeatureFactory geoJsonFeatureFactory;

  private ApplicationPipelineGeoJsonViewFactory viewFactory;

  private PwaApplicationDetail pwaApplicationDetail;

  private final CoordinatePair validFromLocationCoordinates = CoordinatePairTestUtil.getDefaultCoordinate(1, 2);
  private final CoordinatePair validToLocationCoordinates = CoordinatePairTestUtil.getDefaultCoordinate(3, 4);



  @Before
  public void setUp() throws Exception {
    viewFactory = new ApplicationPipelineGeoJsonViewFactory(pipelineService, geoJsonFeatureFactory);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(geoJsonFeatureFactory.createFeatureCollection(any()))
        .thenAnswer(invocation ->  GeoJsonFeatureCollectionTestUtil.createCollectionFrom((List<GeoJsonFeature>)invocation.getArgument(0)));
    when(geoJsonFeatureFactory.createSimpleLineFeature(any(), any()))
        .thenAnswer(invocation -> GeoJsonFeatureTestUtil.getFakeFeature());
  }

  @Test
  public void createApplicationPipelinesAsLineFeatures_hasPipeline_andPipelineCoordinatesValid_andPipelineOnSeabed() {

    var pipelineOverview = new CustomPipelineOverview(
        "PL1",
        "FROM",
        validFromLocationCoordinates,
        "TO",
        validToLocationCoordinates,
        BigDecimal.TEN,
        PipelineStatus.IN_SERVICE);

    when(pipelineService.getApplicationPipelineOverviews(any()))
        .thenReturn(List.of(pipelineOverview));

    var pipelineGeoJsonFeatures = viewFactory.createApplicationPipelinesAsLineFeatures(pwaApplicationDetail);

    verify(pipelineService).getApplicationPipelineOverviews(pwaApplicationDetail);
    verify(geoJsonFeatureFactory).createSimpleLineFeature(validFromLocationCoordinates, validToLocationCoordinates);

    assertThat(pipelineGeoJsonFeatures.getFeatures()).hasOnlyOneElementSatisfying(geoJsonFeature -> {
      assertThat(geoJsonFeature.getProperties()).isNotEmpty();
    });

  }

  @Test
  public void createApplicationPipelinesAsLineFeatures_hasPipeline_andPipelineCoordinatesValid_andPipelineReturnedToShore() {

    var pipelineOverview = new CustomPipelineOverview(
        "PL1",
        "FROM",
        validFromLocationCoordinates,
        "TO",
        validToLocationCoordinates,
        BigDecimal.TEN,
        PipelineStatus.RETURNED_TO_SHORE);

    when(pipelineService.getApplicationPipelineOverviews(any()))
        .thenReturn(List.of(pipelineOverview));

    var pipelineGeoJsonFeatures = viewFactory.createApplicationPipelinesAsLineFeatures(pwaApplicationDetail);

    verify(pipelineService).getApplicationPipelineOverviews(pwaApplicationDetail);

    assertThat(pipelineGeoJsonFeatures.getFeatures()).isEmpty();

  }

  @Test
  public void createApplicationPipelinesAsLineFeatures_hasPipeline_andPipelineCoordinatesInvalid_andPipelineOnSeabed() {

    var pipelineOverview = new CustomPipelineOverview(
        "PL1",
        "FROM",
        CoordinatePairTestUtil.getNullCoordinate(),
        "TO",
        CoordinatePairTestUtil.getNullCoordinate(),
        BigDecimal.TEN,
        PipelineStatus.IN_SERVICE);

    when(pipelineService.getApplicationPipelineOverviews(any()))
        .thenReturn(List.of(pipelineOverview));

    var pipelineGeoJsonFeatures = viewFactory.createApplicationPipelinesAsLineFeatures(pwaApplicationDetail);

    verify(pipelineService).getApplicationPipelineOverviews(pwaApplicationDetail);

    assertThat(pipelineGeoJsonFeatures.getFeatures()).isEmpty();

  }

  private static class CustomPipelineOverview implements PipelineOverview {

    private final String pipelineRef;

    private final String fromLocation;
    private final String toLocation;

    private final BigDecimal length;

    private final CoordinatePair fromCoordinates;
    private final CoordinatePair toCoordinates;

    private final PipelineStatus pipelineStatus;

    private CustomPipelineOverview(String pipelineRef,
                                   String fromLocation,
                                   CoordinatePair fromCoordinates,
                                   String toLocation,
                                   CoordinatePair toCoordinates,
                                   BigDecimal length,
                                   PipelineStatus pipelineStatus) {

      this.pipelineRef = pipelineRef;
      this.fromLocation = fromLocation;
      this.toLocation = toLocation;
      this.length = length;
      this.fromCoordinates = fromCoordinates;
      this.toCoordinates = toCoordinates;
      this.pipelineStatus = pipelineStatus;
    }


    @Override
    public Integer getPipelineId() {
      return null;
    }

    @Override
    public PipelineType getPipelineType() {
      return null;
    }

    @Override
    public Boolean getPipelineInBundle() {
      return null;
    }

    @Override
    public String getPipelineNumber() {
      return pipelineRef;
    }

    @Override
    public Integer getPadPipelineId() {
      return null;
    }

    @Override
    public String getFromLocation() {
      return fromLocation;
    }

    @Override
    public CoordinatePair getFromCoordinates() {
      return fromCoordinates;
    }

    @Override
    public String getToLocation() {
      return toLocation;
    }

    @Override
    public CoordinatePair getToCoordinates() {
      return toCoordinates;
    }

    @Override
    public String getComponentParts() {
      return null;
    }

    @Override
    public BigDecimal getLength() {
      return length;
    }

    @Override
    public String getProductsToBeConveyed() {
      return null;
    }

    @Override
    public Long getNumberOfIdents() {
      return null;
    }

    @Override
    public BigDecimal getMaxExternalDiameter() {
      return null;
    }

    @Override
    public String getBundleName() {
      return null;
    }

    @Override
    public PipelineFlexibility getPipelineFlexibility() {
      return null;
    }

    @Override
    public PipelineMaterial getPipelineMaterial() {
      return null;
    }

    @Override
    public String getOtherPipelineMaterialUsed() {
      return null;
    }

    @Override
    public Boolean getTrenchedBuriedBackfilled() {
      return null;
    }

    @Override
    public String getTrenchingMethodsDescription() {
      return null;
    }

    @Override
    public PipelineStatus getPipelineStatus() {
      return pipelineStatus;
    }

    @Override
    public String getPipelineStatusReason() {
      return null;
    }

    @Override
    public String getTemporaryPipelineNumber() {
      return null;
    }

    @Override
    public Boolean getAlreadyExistsOnSeabed() {
      return null;
    }

    @Override
    public Boolean getPipelineInUse() {
      return null;
    }

    @Override
    public String getFootnote() {
      return null;
    }
  }
}