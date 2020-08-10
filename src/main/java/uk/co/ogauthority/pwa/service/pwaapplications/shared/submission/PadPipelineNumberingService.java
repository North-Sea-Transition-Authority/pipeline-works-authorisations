package uk.co.ogauthority.pwa.service.pwaapplications.shared.submission;

import com.google.common.annotations.VisibleForTesting;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.repository.submission.PadPipelineSubmissionRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;

@Service
public class PadPipelineNumberingService {

  private final PadPipelineSubmissionRepository padPipelineSubmissionRepository;
  private final ApplicationContext applicationContext;

  @Autowired
  public PadPipelineNumberingService(
      PadPipelineSubmissionRepository padPipelineSubmissionRepository,
      ApplicationContext applicationContext) {
    this.padPipelineSubmissionRepository = padPipelineSubmissionRepository;
    this.applicationContext = applicationContext;
  }

  @Transactional
  public void assignPipelineReferences(PwaApplicationDetail detail) {
    var pipelinesService = ApplicationTask.PIPELINES.getServiceClass();
    boolean hasPipelinesTask = applicationContext.getBean(pipelinesService).canShowInTaskList(detail);
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

  @VisibleForTesting
  void attachNewReference(PadPipeline padPipeline) {
    var referenceNumber = padPipelineSubmissionRepository.getNextPipelineReferenceNumber();
    padPipeline.setPipelineRef(padPipeline.getCoreType().getReferencePrefix() + referenceNumber);
  }
}
