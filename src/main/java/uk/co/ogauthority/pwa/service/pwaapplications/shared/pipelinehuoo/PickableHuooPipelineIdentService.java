package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailIdentService;

@Service
public class PickableHuooPipelineIdentService {

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;

  private final PipelineDetailIdentService pipelineDetailIdentService;

  @Autowired
  public PickableHuooPipelineIdentService(
      PadPipelineService padPipelineService,
      PadPipelineIdentService padPipelineIdentService,
      PipelineDetailIdentService pipelineDetailIdentService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.pipelineDetailIdentService = pipelineDetailIdentService;
  }


  // helper to provide ident views for pipeline from either the current application or consented model if not imported.
  private List<IdentView> getPickablePipelineIdentViews(PwaApplicationDetail pwaApplicationDetail,
                                                        PipelineId pipelineId) {
    var padPipeline = padPipelineService.findByPwaApplicationDetailAndPipelineId(pwaApplicationDetail, pipelineId);
    if (padPipeline.isPresent()) {
      return padPipelineIdentService.getIdentViews(padPipeline.get());
    }

    return pipelineDetailIdentService.getSortedPipelineIdentViewsForPipeline(pipelineId);
  }

  /**
   *  <p>This will return idents from either the application or consented model for a given pipeline ID.
   *  If the application detail does not contain an application version of the pipeline, then the consented model
   *  ident will be returned.</p>
   *
   *  <p>It is not the responsibility of this method to check that the application detail is for the same masterPwa consented pipeline.</p>
   */
  public List<PickableIdentLocationOption> getSortedPickableIdentLocationOptions(PwaApplicationDetail pwaApplicationDetail,
                                                                                 PipelineId pipelineId) {
    return getPickablePipelineIdentViews(pwaApplicationDetail, pipelineId)
        .stream()
        .flatMap(identView -> PickableIdentLocationOption.createIdentLocationOptionsFrom(identView).stream())
        .sorted(Comparator.comparing(PickableIdentLocationOption::getSortKey))
        .collect(Collectors.toList());
  }

}
