package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineRemovalService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriter;

/**
 * Deals with pipeline links in other applications for pipelines that have been moved to a retried state by this application.
 * Removes both OrgUserGroup Links to the associated pipeline and PadPipeline objects from any application still in an updatable status.
 * Stops an error where the tasklist throws an exception if one of the modified pipelines has been retired in another application.
 */
@Service
public class RetiredPipelineWriter implements ConsentWriter {
  private final PadOrganisationRoleService padOrganisationRoleService;

  private final PadPipelineService padPipelineService;

  private final PipelineRemovalService pipelineRemovalService;

  private final Set<PipelineStatus> retiredPipelineStatuses = PipelineStatus.getStatusesWithoutState(PhysicalPipelineState.ON_SEABED);
  private final Set<PwaApplicationStatus> affectedApplicationStatuses = PwaApplicationStatus.updatableStatuses();

  @Autowired
  public RetiredPipelineWriter(PadOrganisationRoleService padOrganisationRoleService,
                               PadPipelineService padPipelineService, PipelineRemovalService pipelineRemovalService) {
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.padPipelineService = padPipelineService;
    this.pipelineRemovalService = pipelineRemovalService;
  }

  @Override
  public int getExecutionOrder() {
    return 100;
  }

  @Override
  public boolean writerIsApplicable(Collection<ApplicationTask> applicationTaskSet, PwaConsent pwaConsent) {
    return applicationTaskSet.contains(ApplicationTask.PIPELINES);
  }

  @Override
  public ConsentWriterDto write(PwaApplicationDetail pwaApplicationDetail, PwaConsent pwaConsent,
                                ConsentWriterDto consentWriterDto) {

    //Get pipelines that have been retired by this application.
    var applicationPipelines = consentWriterDto.getPipelineToNewDetailMap();
    var pipelineDetails = applicationPipelines
        .values()
        .stream()
        .filter(pipelineDetail -> retiredPipelineStatuses.contains(pipelineDetail.getPipelineStatus()))
        .collect(Collectors.toList());
    if (pipelineDetails.isEmpty()) {
      //No retired pipelines, no work to do.
      return consentWriterDto;
    }

    //Remove Org Group Links for Retired Pipelines in other applications.
    padOrganisationRoleService.removePipelineLinksForRetiredPipelines(
        applicationPipelines
            .entrySet()
            .stream()
            .filter(detail -> retiredPipelineStatuses.contains(detail.getValue().getPipelineStatus()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList()));

    //Remove PadPipeline objects for retired pipelines in other applications.
    for (var retiredPipeline : pipelineDetails) {
      var pipelinesToRemove = padPipelineService.findSubmittedOrDraftPipelinesWithPipelineNumber(retiredPipeline.getPipelineNumber())
          .stream()
          .filter(padPipeline -> affectedApplicationStatuses.contains(padPipeline.getPwaApplicationDetail().getStatus()))
          .collect(Collectors.toList());
      pipelinesToRemove.forEach(pipelineRemovalService::removePipeline);
    }
    return consentWriterDto;
  }
}
