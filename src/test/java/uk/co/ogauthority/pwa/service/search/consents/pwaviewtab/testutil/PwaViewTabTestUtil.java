package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.testutil;

import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

public class PwaViewTabTestUtil {


  private PwaViewTabTestUtil(){}




  public static PipelineOverview createPipelineOverview(String referenceId) {

    var pipelineDetail =  new PipelineDetail();
    var pipeline = new Pipeline();
    pipeline.setId(1);

    try {
      PadPipeline padPipeline = PadPipelineTestUtil.createPadPipeline(new PwaApplicationDetail(), pipeline, PipelineType.PRODUCTION_FLOWLINE);
      pipelineDetail.setPipeline(pipeline);
      pipelineDetail.setPipelineNumber(referenceId);
      pipelineDetail.setPipelineStatus(padPipeline.getPipelineStatus());
      pipelineDetail.setFromLocation(padPipeline.getFromLocation());
      pipelineDetail.setFromCoordinates(padPipeline.getFromCoordinates());
      pipelineDetail.setToLocation(padPipeline.getToLocation());
      pipelineDetail.setToCoordinates(padPipeline.getToCoordinates());
      pipelineDetail.setLength(padPipeline.getLength());

    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return new PipelineHeaderView(pipelineDetail);
  }

}
