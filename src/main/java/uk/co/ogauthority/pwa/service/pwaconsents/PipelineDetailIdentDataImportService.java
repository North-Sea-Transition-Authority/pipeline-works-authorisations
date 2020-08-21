package uk.co.ogauthority.pwa.service.pwaconsents;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;

@Service
public class PipelineDetailIdentDataImportService {

  private final PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository;
  private final PadPipelineIdentService padPipelineIdentService;
  private final PadPipelineIdentDataService padPipelineIdentDataService;

  @Autowired
  public PipelineDetailIdentDataImportService(
      PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository,
      PadPipelineIdentService padPipelineIdentService,
      PadPipelineIdentDataService padPipelineIdentDataService) {
    this.pipelineDetailIdentDataRepository = pipelineDetailIdentDataRepository;
    this.padPipelineIdentService = padPipelineIdentService;
    this.padPipelineIdentDataService = padPipelineIdentDataService;
  }

  public void importIdentsAndData(PipelineDetail pipelineDetail, PadPipeline padPipeline) {
    Map<PipelineDetailIdent, List<PipelineDetailIdentData>> detailIdentMap;
    detailIdentMap = pipelineDetailIdentDataRepository.getAllByPipelineDetailIdent_PipelineDetail(pipelineDetail)
        .stream()
        .collect(Collectors.groupingBy(PipelineDetailIdentData::getPipelineDetailIdent));

    Map<PadPipelineIdent, List<PadPipelineIdentData>> builtIdentMap = detailIdentMap.entrySet()
        .stream()
        .map(entry -> buildIdentAndData(padPipeline, entry.getKey(), entry.getValue()))
        .flatMap(Collection::stream)
        .collect(Collectors.groupingBy(PadPipelineIdentData::getPadPipelineIdent));

    List<PadPipelineIdent> pipelineIdents = builtIdentMap.keySet()
        .stream()
        .collect(Collectors.toUnmodifiableList());

    List<PadPipelineIdentData> pipelineIdentData = builtIdentMap.entrySet()
        .stream()
        .flatMap(padPipelineIdentListEntry -> padPipelineIdentListEntry.getValue().stream())
        .collect(Collectors.toUnmodifiableList());

    padPipelineIdentService.saveAll(pipelineIdents);
    padPipelineIdentDataService.saveAll(pipelineIdentData);

  }

  @VisibleForTesting
  public List<PadPipelineIdentData> buildIdentAndData(PadPipeline padPipeline, PipelineDetailIdent detailIdent,
                                                      List<PipelineDetailIdentData> identDataList) {
    var padPipelineIdent = mapIdentToPadPipelineIdent(padPipeline, detailIdent);
    return mapIdentsToPadPipelineIdents(padPipelineIdent, identDataList);
  }

  @VisibleForTesting
  public PadPipelineIdent mapIdentToPadPipelineIdent(PadPipeline padPipeline, PipelineDetailIdent detailIdent) {
    var ident = new PadPipelineIdent(padPipeline, detailIdent.getIdentNo());
    ident.setLength(detailIdent.getLength());
    ident.setToLocation(detailIdent.getToLocation());
    ident.setFromLocation(detailIdent.getFromLocation());

    var fromCoordinates = new CoordinatePair(
        new LatitudeCoordinate(detailIdent.getFromLatitudeDegrees(), detailIdent.getFromLatitudeMinutes(),
            detailIdent.getFromLatitudeSeconds(), detailIdent.getFromLatitudeDirection()),
        new LongitudeCoordinate(detailIdent.getFromLongitudeDegrees(), detailIdent.getFromLongitudeMinutes(),
            detailIdent.getFromLongitudeSeconds(), detailIdent.getFromLongitudeDirection())
    );

    var toCoordinates = new CoordinatePair(
        new LatitudeCoordinate(detailIdent.getToLatitudeDegrees(), detailIdent.getToLatitudeMinutes(),
            detailIdent.getToLatitudeSeconds(), detailIdent.getToLatitudeDirection()),
        new LongitudeCoordinate(detailIdent.getToLongitudeDegrees(), detailIdent.getToLongitudeMinutes(),
            detailIdent.getToLongitudeSeconds(), detailIdent.getToLongitudeDirection())
    );

    ident.setFromCoordinates(fromCoordinates);
    ident.setToCoordinates(toCoordinates);

    return ident;
  }

  @VisibleForTesting
  public List<PadPipelineIdentData> mapIdentsToPadPipelineIdents(PadPipelineIdent ident,
                                                                 List<PipelineDetailIdentData> dataList) {
    return dataList.stream()
        .map(detailIdentData -> mapIdentDataToPadPipelineIdentData(ident, detailIdentData))
        .collect(Collectors.toUnmodifiableList());
  }

  @VisibleForTesting
  public PadPipelineIdentData mapIdentDataToPadPipelineIdentData(PadPipelineIdent ident, PipelineDetailIdentData identData) {
    var padPipelineIdentData = new PadPipelineIdentData(ident);
    padPipelineIdentData.setComponentPartsDesc(identData.getComponentPartsDesc());
    padPipelineIdentData.setExternalDiameter(identData.getExternalDiameter());
    padPipelineIdentData.setInternalDiameter(identData.getInternalDiameter());
    padPipelineIdentData.setWallThickness(identData.getWallThickness());
    padPipelineIdentData.setInsulationCoatingType(identData.getInsulationCoatingType());
    padPipelineIdentData.setMaop(identData.getMaop());
    padPipelineIdentData.setProductsToBeConveyed(identData.getProductsToBeConveyed());
    // TODO: PWA-682 - Add MultiCore values to PipelineDetailIdentData
    // Set multi core values
    return padPipelineIdentData;
  }

}
