package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;

@Service
public class PipelineDetailIdentDataImportService {

  private final PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository;
  private final PadPipelineIdentService padPipelineIdentService;
  private final PadPipelineIdentDataService padPipelineIdentDataService;
  private final PipelineIdentDataMappingService pipelineIdentDataMappingService;
  private final PipelineIdentMappingService pipelineIdentMappingService;

  @Autowired
  public PipelineDetailIdentDataImportService(
      PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository,
      PadPipelineIdentService padPipelineIdentService,
      PadPipelineIdentDataService padPipelineIdentDataService,
      PipelineIdentDataMappingService pipelineIdentDataMappingService,
      PipelineIdentMappingService pipelineIdentMappingService) {
    this.pipelineDetailIdentDataRepository = pipelineDetailIdentDataRepository;
    this.padPipelineIdentService = padPipelineIdentService;
    this.padPipelineIdentDataService = padPipelineIdentDataService;
    this.pipelineIdentDataMappingService = pipelineIdentDataMappingService;
    this.pipelineIdentMappingService = pipelineIdentMappingService;
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

    pipelineIdentMappingService.mapIdent(ident, detailIdent);

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

    pipelineIdentDataMappingService.mapPipelineIdentData(padPipelineIdentData, identData);

    return padPipelineIdentData;
  }

}
