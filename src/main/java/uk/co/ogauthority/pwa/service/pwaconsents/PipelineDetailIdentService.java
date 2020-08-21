package uk.co.ogauthority.pwa.service.pwaconsents;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineIdent;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.util.StreamUtils;

/**
 * Retrieve and persist ident data for the consented model.
 */
@Service
public class PipelineDetailIdentService {
  private final PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository;
  private final PipelineDetailIdentRepository pipelineDetailIdentRepository;

  @Autowired
  public PipelineDetailIdentService(PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository,
                                    PipelineDetailIdentRepository pipelineDetailIdentRepository) {
    this.pipelineDetailIdentDataRepository = pipelineDetailIdentDataRepository;
    this.pipelineDetailIdentRepository = pipelineDetailIdentRepository;
  }

  /**
   * Processing steps recreate method used for creating Idents views for a PadPipeline.
   */
  public List<IdentView> getSortedPipelineIdentViewsForPipeline(PipelineId pipelineId) {
    var idents = pipelineDetailIdentRepository.findByPipelineDetail_Pipeline_IdInAndPipelineDetail_tipFlagIsTrue(
        Set.of(pipelineId.asInt())
    );

    var identDataMap = getDataFromIdentList(idents);

    return identDataMap.keySet()
        .stream()
        .sorted(Comparator.comparing(PipelineIdent::getIdentNo))
        .map(ident -> new IdentView(identDataMap.get(ident)))
        .collect(Collectors.toUnmodifiableList());

  }

  private Map<PipelineDetailIdent, PipelineDetailIdentData> getDataFromIdentList(List<PipelineDetailIdent> identList) {
    return pipelineDetailIdentDataRepository.getAllByPipelineDetailIdentIn(identList)
        .stream()
        .collect(StreamUtils.toLinkedHashMap(PipelineDetailIdentData::getPipelineDetailIdent, data -> data));
  }

}
