package uk.co.ogauthority.pwa.service.pwaapplications.shared.submission;

import com.google.common.annotations.VisibleForTesting;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.repository.submission.PadPipelineSubmissionRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationTaskService;

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
      var nonConsentedPipelines = padPipelineSubmissionRepository.getNonConsentedPipelines(detail);
      nonConsentedPipelines.forEach(padPipeline -> {
        padPipeline.setTemporaryRef(padPipeline.getPipelineRef());
        attachNewReference(padPipeline);
      });
      if (nonConsentedPipelines.size() > 0) {
        padPipelineSubmissionRepository.saveAll(nonConsentedPipelines);
      }
    }
  }

  public boolean nonConsentedPadPipelineRequiresFullReference(PadPipeline padPipeline){
    return StringUtils.isEmpty(padPipeline.getTemporaryRef());
  }

  @VisibleForTesting
  void attachNewReference(PadPipeline padPipeline) {
    var referenceNumber = padPipelineSubmissionRepository.getNextPipelineReferenceNumber();
    padPipeline.setPipelineRef(padPipeline.getCoreType().getReferencePrefix() + referenceNumber);
  }
}
