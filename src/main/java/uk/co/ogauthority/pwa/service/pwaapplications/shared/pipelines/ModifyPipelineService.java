package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.ModifyPipelineForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.NamedPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.NamedPipelineDto;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailIdentDataImportService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;

/**
 * This service is used to both import and show consented pipeline data available to the current app detail.
 */
@Service
public class ModifyPipelineService {

  private final PipelineService pipelineService;
  private final PadPipelineService padPipelineService;
  private final PipelineDetailService pipelineDetailService;
  private final PipelineDetailIdentDataImportService pipelineDetailIdentDataImportService;

  @Autowired
  public ModifyPipelineService(
      PipelineService pipelineService,
      PadPipelineService padPipelineService,
      PipelineDetailService pipelineDetailService,
      PipelineDetailIdentDataImportService pipelineDetailIdentDataImportService) {
    this.pipelineService = pipelineService;
    this.padPipelineService = padPipelineService;
    this.pipelineDetailService = pipelineDetailService;
    this.pipelineDetailIdentDataImportService = pipelineDetailIdentDataImportService;
  }

  @VisibleForTesting
  public List<PipelineDetail> getConsentedPipelinesNotOnApplication(PwaApplicationDetail pwaApplicationDetail) {
    var consentedPipelines = pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(
        pwaApplicationDetail.getMasterPwaApplication());
    var pipelineIdsContainedInApplication = padPipelineService.getPipelines(pwaApplicationDetail).stream()
        .map(PadPipeline::getPipelineId)
        .collect(Collectors.toSet());

    return consentedPipelines.stream()
        .filter(pipelineDetail -> !pipelineIdsContainedInApplication.contains(pipelineDetail.getPipelineId()))
        .collect(Collectors.toUnmodifiableList());
  }

  public List<NamedPipeline> getSelectableConsentedPipelines(PwaApplicationDetail pwaApplicationDetail) {
    return getConsentedPipelinesNotOnApplication(pwaApplicationDetail).stream()
        .map(NamedPipelineDto::fromPipelineDetail)
        .collect(Collectors.toUnmodifiableList());
  }

  @Transactional
  public void importPipeline(PwaApplicationDetail detail, ModifyPipelineForm form) {
    var pipelineId = Integer.parseInt(form.getPipelineId());
    var pipelineDetail = pipelineDetailService.getLatestByPipelineId(pipelineId);
    var padPipeline = padPipelineService.copyDataToNewPadPipeline(detail, pipelineDetail);
    pipelineDetailIdentDataImportService.importIdentsAndData(pipelineDetail, padPipeline);
  }
}
