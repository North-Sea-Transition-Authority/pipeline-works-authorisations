package uk.co.ogauthority.pwa.service.pwaconsents;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;

@Service
public class PipelineDetailService {

  private final PipelineDetailRepository pipelineDetailRepository;

  public PipelineDetailService(
      PipelineDetailRepository pipelineDetailRepository) {
    this.pipelineDetailRepository = pipelineDetailRepository;
  }

  public List<PipelineBundlePairDto> getSimilarPipelineBundleNamesByDetail(PwaApplicationDetail detail) {
    return pipelineDetailRepository.getBundleNamesByPwaApplicationDetail(detail);
  }
}
