package uk.co.ogauthority.pwa.service.pwaconsents;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.pipelinedatautils.PipelineIdentViewCollectorService;

/**
 * Retrieve and persist ident data for the consented model.
 */
@Service
public class PipelineDetailIdentService {
  private final PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository;
  private final PipelineDetailIdentRepository pipelineDetailIdentRepository;
  private final PipelineIdentViewCollectorService pipelineIdentViewCollectorService;

  @Autowired
  public PipelineDetailIdentService(PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository,
                                    PipelineDetailIdentRepository pipelineDetailIdentRepository,
                                    PipelineIdentViewCollectorService pipelineIdentViewCollectorService) {
    this.pipelineDetailIdentDataRepository = pipelineDetailIdentDataRepository;
    this.pipelineDetailIdentRepository = pipelineDetailIdentRepository;
    this.pipelineIdentViewCollectorService = pipelineIdentViewCollectorService;
  }

  /**
   * Processing steps recreate method used for creating Idents views for a PadPipeline.
   */
  @Transactional(readOnly = true) // just a hint, not guaranteed to be enforced read only.
  public List<IdentView> getSortedPipelineIdentViewsForPipeline(PipelineId pipelineId) {
    return getMapOfPipelineIdToSortedIdentViewList(List.of(pipelineId)).get(pipelineId);
  }

  @Transactional(readOnly = true) // just a hint, not guaranteed to be enforced read only.
  public Map<PipelineId, List<IdentView>> getSortedPipelineIdentViewsForPipelines(Collection<PipelineId> pipelineIds) {
    return getMapOfPipelineIdToSortedIdentViewList(pipelineIds);
  }

  private Map<PipelineId, List<IdentView>> getMapOfPipelineIdToSortedIdentViewList(Collection<PipelineId> pipelineIds) {
    return pipelineIdentViewCollectorService.getPipelineIdToIdentVewsMap(
        PipelineDetailIdent.class,
        PipelineDetailIdentData.class,
        () -> pipelineDetailIdentRepository.findByPipelineDetail_Pipeline_IdInAndPipelineDetail_tipFlagIsTrue(
            pipelineIds.stream()
                .map(PipelineId::asInt)
                .collect(toSet())
        ),
        pipelineDetailIdentDataRepository::getAllByPipelineDetailIdentIn
    );
  }

}
