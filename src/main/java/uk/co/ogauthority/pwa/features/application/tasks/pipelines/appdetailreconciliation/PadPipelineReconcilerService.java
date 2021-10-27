package uk.co.ogauthority.pwa.features.application.tasks.pipelines.appdetailreconciliation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Given application details, reconcile PadPipelines between versions by comparing the root Pipeline.
 */
@Service
public class PadPipelineReconcilerService {

  private final PadPipelineService padPipelineService;

  @Autowired
  public PadPipelineReconcilerService(PadPipelineService padPipelineService) {
    this.padPipelineService = padPipelineService;
  }


  public ReconciledApplicationDetailPadPipelines reconcileApplicationDetailPadPipelines(PwaApplicationDetail sourcePwaApplicationDetail,
                                                                                        PwaApplicationDetail detailToReconcile) {
    var sourceDetailPadPipelines = padPipelineService.getPipelines(sourcePwaApplicationDetail);
    var toReconcileDetailPadPipelines = padPipelineService.getPipelines(detailToReconcile);


    return new ReconciledApplicationDetailPadPipelines(
        sourcePwaApplicationDetail,
        detailToReconcile,
        sourceDetailPadPipelines,
        toReconcileDetailPadPipelines
    );

  }
}
