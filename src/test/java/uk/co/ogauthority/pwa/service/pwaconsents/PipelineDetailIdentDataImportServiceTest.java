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
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.location.Coordinate;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;

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
    pipelineDetailIdentDataImportService = new PipelineDetailIdentDataImportService(pipelineDetailIdentDataRepository,
        padPipelineIdentService, padPipelineIdentDataService);
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
    detailIdent.setFromLatitudeDegrees(1);
    detailIdent.setFromLatitudeMinutes(1);
    detailIdent.setFromLatitudeSeconds(BigDecimal.ONE);
    detailIdent.setFromLatitudeDirection(LatitudeDirection.NORTH);
    detailIdent.setFromLongitudeDegrees(2);
    detailIdent.setFromLongitudeMinutes(2);
    detailIdent.setFromLongitudeSeconds(BigDecimal.valueOf(2));
    detailIdent.setFromLongitudeDirection(LongitudeDirection.EAST);
    detailIdent.setToLocation("to");
    detailIdent.setToLatitudeDegrees(3);
    detailIdent.setToLatitudeMinutes(3);
    detailIdent.setToLatitudeSeconds(BigDecimal.valueOf(3));
    detailIdent.setToLatitudeDirection(LatitudeDirection.SOUTH);
    detailIdent.setToLongitudeDegrees(4);
    detailIdent.setToLongitudeMinutes(4);
    detailIdent.setToLongitudeSeconds(BigDecimal.valueOf(4));
    detailIdent.setToLongitudeDirection(LongitudeDirection.WEST);

    var result = pipelineDetailIdentDataImportService.mapIdentToPadPipelineIdent(padPipeline, detailIdent);

    assertThat(result.getPadPipeline()).isEqualTo(padPipeline);

    assertThat(result.getIdentNo()).isEqualTo(detailIdent.getIdentNo());
    assertThat(result.getFromLocation()).isEqualTo(detailIdent.getFromLocation());
    assertThat(result.getToLocation()).isEqualTo(detailIdent.getToLocation());
    assertThat(result.getLength()).isEqualTo(detailIdent.getLength());

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