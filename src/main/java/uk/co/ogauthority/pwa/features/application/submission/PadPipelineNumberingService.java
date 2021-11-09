package uk.co.ogauthority.pwa.features.application.submission;

import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTaskService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class PadPipelineNumberingService {

  private final PadPipelineSubmissionRepository padPipelineSubmissionRepository;
  private final ApplicationTaskService applicationTaskService;

  @Autowired
  public PadPipelineNumberingService(
      PadPipelineSubmissionRepository padPipelineSubmissionRepository,
      ApplicationTaskService applicationTaskService) {
    this.padPipelineSubmissionRepository = padPipelineSubmissionRepository;
    this.applicationTaskService = applicationTaskService;
  }

  @Transactional
  public void assignPipelineReferences(PwaApplicationDetail detail) {
    var hasPipelinesTask = applicationTaskService.canShowTask(ApplicationTask.PIPELINES, detail);

    if (hasPipelinesTask) {
      var nonConsentedPipelinesRequiringPipelineReference = padPipelineSubmissionRepository.getNonConsentedPipelines(detail)
          .stream()
          .filter(this::nonConsentedPadPipelineRequiresFullReference)
          .collect(toList());
      nonConsentedPipelinesRequiringPipelineReference.forEach(this::attachNewReference);
      if (nonConsentedPipelinesRequiringPipelineReference.size() > 0) {
        padPipelineSubmissionRepository.saveAll(nonConsentedPipelinesRequiringPipelineReference);
      }
    }
  }

  private void setPipelineRefAndStoreTemporaryRefIfUnset(PadPipeline padPipeline, String pipelineRef) {

    if (padPipeline.getTemporaryRef() == null) {
      padPipeline.setTemporaryRef(padPipeline.getPipelineRef());
    }

    padPipeline.setPipelineRef(pipelineRef);
  }

  public boolean nonConsentedPadPipelineRequiresFullReference(PadPipeline padPipeline) {
    return StringUtils.isEmpty(padPipeline.getTemporaryRef());
  }

  @Transactional
  public void setManualPipelineReference(PadPipeline padPipeline, String pipelineReference) {
    setPipelineRefAndStoreTemporaryRefIfUnset(padPipeline, pipelineReference);
    padPipelineSubmissionRepository.save(padPipeline);
  }

  @VisibleForTesting
  void attachNewReference(PadPipeline padPipeline) {
    var referenceNumber = padPipelineSubmissionRepository.getNextPipelineReferenceNumber();
    var newPipelineRef = padPipeline.getCoreType().getReferencePrefix() + referenceNumber;
    setPipelineRefAndStoreTemporaryRefIfUnset(padPipeline, newPipelineRef);
  }
}
