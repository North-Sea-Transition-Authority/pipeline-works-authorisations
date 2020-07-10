package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;

@Service
public class PadPipelinePersisterService {

  private final PadPipelineRepository padPipelineRepository;
  private final PadPipelineIdentRepository padPipelineIdentRepository;

  public PadPipelinePersisterService(
      PadPipelineRepository padPipelineRepository,
      PadPipelineIdentRepository padPipelineIdentRepository) {
    this.padPipelineRepository = padPipelineRepository;
    this.padPipelineIdentRepository = padPipelineIdentRepository;
  }

  public void savePipeline(PadPipeline padPipeline) {
    padPipelineRepository.save(padPipeline);
  }



}
