package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.model.location.Coordinate;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentDataImportService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineIdentDataMappingService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineIdentMappingService;

@RunWith(MockitoJUnitRunner.class)
public class PipelineDetailIdentDataImportServiceTest {

  @Mock
  private PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PadPipelineIdentDataService padPipelineIdentDataService;

  @Captor
  private ArgumentCaptor<List<PadPipelineIdent>> identCaptor;

  @Captor
  private ArgumentCaptor<List<PadPipelineIdentData>> identDataCaptor;

  private PipelineDetailIdentDataImportService pipelineDetailIdentDataImportService;

  private PadPipeline padPipeline;
  private PipelineDetail pipelineDetail;
  private PipelineDetailIdent pipelineDetailIdent;
  private PipelineDetailIdentData pipelineDetailIdentData;

  @Before
  public void setUp() {

    var identMappingService = new PipelineIdentMappingService();
    var identDataMappingService = new PipelineIdentDataMappingService();

    pipelineDetailIdentDataImportService = new PipelineDetailIdentDataImportService(pipelineDetailIdentDataRepository,
        padPipelineIdentService, padPipelineIdentDataService, identDataMappingService, identMappingService);
    padPipeline = new PadPipeline();
    pipelineDetail = new PipelineDetail();
    pipelineDetailIdent = new PipelineDetailIdent();
    pipelineDetailIdent.setPipelineDetail(pipelineDetail);
    pipelineDetailIdent.setIdentNo(1);
    pipelineDetailIdentData = new PipelineDetailIdentData();
    pipelineDetailIdentData.setPipelineDetailIdent(pipelineDetailIdent);
  }

  @Test
  public void importIdentsAndData_serviceInteraction() {

    when(pipelineDetailIdentDataRepository.getAllByPipelineDetailIdent_PipelineDetail(pipelineDetail))
        .thenReturn(List.of(pipelineDetailIdentData));

    pipelineDetailIdentDataImportService.importIdentsAndData(pipelineDetail, padPipeline);

    verify(padPipelineIdentService, times(1)).saveAll(identCaptor.capture());
    verify(padPipelineIdentDataService, times(1)).saveAll(identDataCaptor.capture());

    assertThat(identCaptor.getValue()).extracting(PadPipelineIdent::getPadPipeline)
        .containsExactly(padPipeline);

    assertThat(identDataCaptor.getValue()).extracting(identData -> identData.getPadPipelineIdent().getPadPipeline())
        .containsExactly(padPipeline);
  }

  @Test
  public void buildIdentAndData_ensureLinkedToPadPipeline() {
    var result = pipelineDetailIdentDataImportService.buildIdentAndData(padPipeline, pipelineDetailIdent,
        List.of(pipelineDetailIdentData));
    assertThat(result).extracting(PadPipelineIdentData::getPadPipelineIdent).extracting(
        PadPipelineIdent::getPadPipeline)
        .containsExactly(padPipeline);
  }

  @Test
  public void mapIdentsToPadPipelineIdents_ensureDataLinkedToPadPipelineIdent() {
    var pipelineIdent = new PadPipelineIdent();
    var data = new PipelineDetailIdentData();
    var result = pipelineDetailIdentDataImportService.mapIdentsToPadPipelineIdents(pipelineIdent, List.of(data));
    assertThat(result).extracting(PadPipelineIdentData::getPadPipelineIdent).containsExactly(pipelineIdent);
  }

  @Test
  public void mapIdentToPadPipelineIdent_assertMappedValues() {
    var detailIdent = new PipelineDetailIdent();
    detailIdent.setIdentNo(1);

    detailIdent.setLength(BigDecimal.ONE);

    detailIdent.setFromLocation("from");
    detailIdent.setFromCoordinates(new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ONE, LatitudeDirection.NORTH),
        new LongitudeCoordinate(2, 2, BigDecimal.valueOf(2), LongitudeDirection.EAST)
    ));

    detailIdent.setToLocation("to");
    detailIdent.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(3, 3, BigDecimal.valueOf(3), LatitudeDirection.SOUTH),
        new LongitudeCoordinate(4, 4, BigDecimal.valueOf(4), LongitudeDirection.WEST)
    ));

    detailIdent.setDefiningStructure(true);

    var result = pipelineDetailIdentDataImportService.mapIdentToPadPipelineIdent(padPipeline, detailIdent);

    assertThat(result.getPadPipeline()).isEqualTo(padPipeline);

    assertThat(result.getIdentNo()).isEqualTo(detailIdent.getIdentNo());
    assertThat(result.getFromLocation()).isEqualTo(detailIdent.getFromLocation());
    assertThat(result.getToLocation()).isEqualTo(detailIdent.getToLocation());
    assertThat(result.getLength()).isEqualTo(detailIdent.getLength());
    assertThat(result.getIsDefiningStructure()).isEqualTo(detailIdent.getIsDefiningStructure());

    assertThat(result.getFromCoordinates().getLatitude())
        .extracting(Coordinate::getDegrees, Coordinate::getMinutes, Coordinate::getSeconds,
            LatitudeCoordinate::getDirection)
        .containsExactly(1, 1, BigDecimal.ONE, LatitudeDirection.NORTH);

    assertThat(result.getFromCoordinates().getLongitude())
        .extracting(Coordinate::getDegrees, Coordinate::getMinutes, Coordinate::getSeconds,
            LongitudeCoordinate::getDirection)
        .containsExactly(2, 2, BigDecimal.valueOf(2), LongitudeDirection.EAST);

    assertThat(result.getToCoordinates().getLatitude())
        .extracting(Coordinate::getDegrees, Coordinate::getMinutes, Coordinate::getSeconds,
            LatitudeCoordinate::getDirection)
        .containsExactly(3, 3, BigDecimal.valueOf(3), LatitudeDirection.SOUTH);

    assertThat(result.getToCoordinates().getLongitude())
        .extracting(Coordinate::getDegrees, Coordinate::getMinutes, Coordinate::getSeconds,
            LongitudeCoordinate::getDirection)
        .containsExactly(4, 4, BigDecimal.valueOf(4), LongitudeDirection.WEST);
  }

  @Test
  public void mapIdentDataToPadPipelineIdentData() {
    var padPipelineIdent = new PadPipelineIdent();
    var pipelineDetailIdentData = new PipelineDetailIdentData();

    // TODO: PWA-682 - Add test data
    pipelineDetailIdentData.setComponentPartsDesc("desc");
    pipelineDetailIdentData.setExternalDiameter(BigDecimal.ONE);
    pipelineDetailIdentData.setInternalDiameter(BigDecimal.valueOf(2));
    pipelineDetailIdentData.setWallThickness(BigDecimal.valueOf(3));
    pipelineDetailIdentData.setInsulationCoatingType("coating type");
    pipelineDetailIdentData.setMaop(BigDecimal.valueOf(4));
    pipelineDetailIdentData.setProductsToBeConveyed("prod");

    var result = pipelineDetailIdentDataImportService.mapIdentDataToPadPipelineIdentData(padPipelineIdent,
        pipelineDetailIdentData);
    // TODO: PWA-682 - Add assertions
    assertThat(result.getComponentPartsDesc()).isEqualTo(pipelineDetailIdentData.getComponentPartsDesc());
    assertThat(result.getExternalDiameter()).isEqualTo(pipelineDetailIdentData.getExternalDiameter());
    assertThat(result.getInternalDiameter()).isEqualTo(pipelineDetailIdentData.getInternalDiameter());
    assertThat(result.getWallThickness()).isEqualTo(pipelineDetailIdentData.getWallThickness());
    assertThat(result.getInsulationCoatingType()).isEqualTo(pipelineDetailIdentData.getInsulationCoatingType());
    assertThat(result.getMaop()).isEqualTo(pipelineDetailIdentData.getMaop());
    assertThat(result.getProductsToBeConveyed()).isEqualTo(pipelineDetailIdentData.getProductsToBeConveyed());
  }
}