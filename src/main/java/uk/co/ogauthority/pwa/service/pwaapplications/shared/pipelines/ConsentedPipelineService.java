package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class ConsentedPipelineService {

  private final PipelineService pipelineService;
  private final PadPipelineService padPipelineService;

  @Autowired
  public ConsentedPipelineService(
      PipelineService pipelineService,
      PadPipelineService padPipelineService) {
    this.pipelineService = pipelineService;
    this.padPipelineService = padPipelineService;
  }

  public List<PadPipeline> getConsentedPipelinesNotOnApplication(PwaApplicationDetail pwaApplicationDetail) {
    var applicationMasterPipelineIds = padPipelineService.getMasterPipelineIds(pwaApplicationDetail);
    var consentedPipelines = pipelineService.getPipelineDetailsForApplicationMasterPwaByTip(
        pwaApplicationDetail.getPwaApplication(), true);
    List<Integer> nonImportedConsentedPipelineIds = consentedPipelines.stream()
        .filter(pipelineDetail -> applicationMasterPipelineIds.stream()
            .noneMatch(masterPipelineId -> masterPipelineId.equals(pipelineDetail.getPipelineId())))
        .map(PipelineDetail::getPipelineId)
        .collect(Collectors.toUnmodifiableList());
    return padPipelineService.getPadPipelinesByMasterAndIds(pwaApplicationDetail.getMasterPwaApplication(),
        nonImportedConsentedPipelineIds);
  }

  public Map<String, String> getSelectableConsentedPipelines(PwaApplicationDetail pwaApplicationDetail) {
    return getConsentedPipelinesNotOnApplication(pwaApplicationDetail).stream()
        .collect(StreamUtils.toLinkedHashMap(pipeline ->
            String.valueOf(pipeline.getId()), PadPipeline::getPipelineRef));
  }
}
