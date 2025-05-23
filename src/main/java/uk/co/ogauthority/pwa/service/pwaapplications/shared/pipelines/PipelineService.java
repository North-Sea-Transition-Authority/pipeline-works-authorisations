package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineRepository;

@Service
public class PipelineService {

  private final PipelineRepository pipelineRepository;

  public PipelineService(PipelineRepository pipelineRepository) {
    this.pipelineRepository = pipelineRepository;
  }

  @Transactional
  public Pipeline createApplicationPipeline(PwaApplication pwaApplication) {
    var newPipeline = new Pipeline(pwaApplication);
    return pipelineRepository.save(newPipeline);
  }

  public Set<Pipeline> getPipelinesFromIds(Set<PipelineId> pipelineIds) {
    var ids = pipelineIds.stream()
        .map(PipelineId::asInt)
        .collect(toSet());
    return new HashSet<>(IterableUtils.toList(pipelineRepository.findAllById(ids)));
  }

  public Pipeline getPipelineFromId(PipelineId pipelineId) {
    return pipelineRepository.findById(pipelineId.asInt())
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find Pipeline with PipelineId of: %s", pipelineId.asInt())));
  }

}
