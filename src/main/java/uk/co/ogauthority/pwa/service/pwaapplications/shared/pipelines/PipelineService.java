package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineRepository;

@Service
public class PipelineService {

  private final PipelineRepository pipelineRepository;
  private final PipelineDetailRepository pipelineDetailRepository;

  public PipelineService(PipelineRepository pipelineRepository,
                         PipelineDetailRepository pipelineDetailRepository) {
    this.pipelineRepository = pipelineRepository;
    this.pipelineDetailRepository = pipelineDetailRepository;
  }

  @Transactional
  public Pipeline createApplicationPipeline(PwaApplication pwaApplication) {
    var newPipeline = new Pipeline(pwaApplication);
    return pipelineRepository.save(newPipeline);
  }

  public List<PipelineDetail> getActivePipelineDetailsForApplicationMasterPwa(PwaApplication pwaApplication) {
    return pipelineDetailRepository.findAllByPipeline_MasterPwaAndEndTimestampIsNull(
        pwaApplication.getMasterPwa()
    );
  }

  public List<PipelineDetail> getNonDeletedPipelineDetailsForApplicationMasterPwaWithTipFlag(
      PwaApplication pwaApplication,
      Boolean tipFlag) {

    return pipelineDetailRepository.findAllByPipeline_MasterPwaAndTipFlagAndDetailStatusIsNot(
        pwaApplication.getMasterPwa(),
        tipFlag,
        "DELETED"
    );
  }

  public List<PipelineDetail> getActivePipelineDetailsForApplicationMasterPwaById(PwaApplication pwaApplication,
                                                                                  Set<PipelineId> pipelineIds) {
    //revisit if performance is bad
    return pipelineDetailRepository.findAllByPipeline_MasterPwaAndEndTimestampIsNull(
        pwaApplication.getMasterPwa()
    ).stream()
        .filter(pd -> pipelineIds.contains(new PipelineId(pd.getPipelineId())))
        .collect(Collectors.toList());
  }

  public Set<Pipeline> getPipelinesFromIds(Set<PipelineId> pipelineIds) {
    var ids = pipelineIds.stream()
        .map(PipelineId::asInt)
        .collect(toSet());
    return new HashSet<>(IterableUtils.toList(pipelineRepository.findAllById(ids)));
  }
}
