package uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.NamedPipelineDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentDataImportService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

/**
 * This service is used to both import and show consented pipeline data available to the current app detail.
 */
@Service
public class ModifyPipelineService {

  private final PadPipelineService padPipelineService;
  private final PipelineDetailService pipelineDetailService;
  private final PipelineDetailIdentDataImportService pipelineDetailIdentDataImportService;

  private static final EnumSet<PhysicalPipelineState> ON_SEABED_AND_ONSHORE = EnumSet.of(
      PhysicalPipelineState.ON_SEABED,
      PhysicalPipelineState.ONSHORE
  );

  @Autowired
  public ModifyPipelineService(
      PadPipelineService padPipelineService,
      PipelineDetailService pipelineDetailService,
      PipelineDetailIdentDataImportService pipelineDetailIdentDataImportService) {
    this.padPipelineService = padPipelineService;
    this.pipelineDetailService = pipelineDetailService;
    this.pipelineDetailIdentDataImportService = pipelineDetailIdentDataImportService;
  }

  @VisibleForTesting
  public List<PipelineDetail> getConsentedPipelinesNotOnApplication(PwaApplicationDetail pwaApplicationDetail) {
    var consentedPipelines = pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(
        pwaApplicationDetail.getMasterPwa());
    var pipelineIdsContainedInApplication = padPipelineService.getPipelines(pwaApplicationDetail).stream()
        .map(PadPipeline::getPipelineId)
        .collect(Collectors.toSet());

    return consentedPipelines.stream()
        .filter(pipelineDetail -> !pipelineIdsContainedInApplication.contains(pipelineDetail.getPipelineId()))
        .collect(Collectors.toUnmodifiableList());
  }

  public List<NamedPipelineDto> getSelectableConsentedPipelines(PwaApplicationDetail pwaApplicationDetail) {
    return getConsentedPipelinesNotOnApplication(pwaApplicationDetail).stream()
        .filter(pipelineDetail -> ON_SEABED_AND_ONSHORE.contains(pipelineDetail.getPipelineStatus().getPhysicalPipelineState()))
        .sorted(Comparator.comparingInt(pipelineDetail ->
            pipelineDetail.getPipelineStatus().getPhysicalPipelineState().getDisplayOrder()))
        .map(NamedPipelineDto::fromPipelineDetail)
        .collect(Collectors.toUnmodifiableList());
  }

  public List<PipelineStatus> getPipelineServiceStatusesForAppType(PwaApplicationType pwaApplicationType) {

    var pipelineStatuses = PipelineStatus.toOrderedListWithoutHistorical();
    var validAppTypesForTransferredPipelineStatus = Set.of(
        PwaApplicationType.CAT_1_VARIATION, PwaApplicationType.CAT_2_VARIATION, PwaApplicationType.DECOMMISSIONING);
    if (!validAppTypesForTransferredPipelineStatus.contains(pwaApplicationType)) {
      return pipelineStatuses.stream()
          .filter(pipelineStatus -> !pipelineStatus.equals(PipelineStatus.TRANSFERRED))
          .collect(Collectors.toList());
    }
    return pipelineStatuses;
  }

  private void errorIfModifyingTransferredPipeline(PipelineDetail pipelineDetail, PwaApplicationDetail pwaApplicationDetail) {
    if (PipelineStatus.TRANSFERRED.equals(pipelineDetail.getPipelineStatus())) {
      throw new ActionNotAllowedException(String.format(
          "Consented pipeline with TRANSFERRED status cannot be modified for pipeline detail id: %s and appDetail id: %s",
          pipelineDetail.getId(), pwaApplicationDetail.getId()));
    }
  }

  @Transactional
  public PadPipeline importPipeline(PwaApplicationDetail detail, ModifyPipelineForm form) {
    var pipelineId = Integer.parseInt(form.getPipelineId());
    var pipelineDetail = pipelineDetailService.getLatestByPipelineId(pipelineId);
    errorIfModifyingTransferredPipeline(pipelineDetail, detail);
    var padPipeline = padPipelineService.copyDataToNewPadPipeline(detail, pipelineDetail, form);
    pipelineDetailIdentDataImportService.importIdentsAndData(pipelineDetail, padPipeline);

    return padPipeline;
  }
}
