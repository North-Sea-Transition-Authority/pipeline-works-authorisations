package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.testutil;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class PwaViewTabTestUtil {


  private PwaViewTabTestUtil(){}




  public static PipelineOverview createPipelineOverview(String referenceId) {
    PadPipeline padPipeline = createPadPipeline();
    var pipelineDetail = createPipelineDetail(padPipeline, referenceId);
    return new PipelineHeaderView(pipelineDetail);
  }

  public static PipelineOverview createPipelineOverview(String referenceId, PipelineStatus pipelineStatus) {
    PadPipeline padPipeline = createPadPipeline();
    var pipelineDetail = createPipelineDetail(padPipeline, referenceId);
    pipelineDetail.setPipelineStatus(pipelineStatus);
    return new PipelineHeaderView(pipelineDetail);
  }

  private static PadPipeline createPadPipeline() {
    var pipeline = new Pipeline();
    pipeline.setId(1);
    PadPipeline padPipeline = new PadPipeline();

    try {
      padPipeline = PadPipelineTestUtil.createPadPipeline(new PwaApplicationDetail(), pipeline, PipelineType.PRODUCTION_FLOWLINE);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return padPipeline;
  }

  private static PipelineDetail createPipelineDetail(PadPipeline padPipeline, String referenceId) {

    var pipelineDetail =  new PipelineDetail();
    pipelineDetail.setPipeline(padPipeline.getPipeline());
    pipelineDetail.setPipelineNumber(referenceId);
    pipelineDetail.setPipelineStatus(padPipeline.getPipelineStatus());
    pipelineDetail.setFromLocation(padPipeline.getFromLocation());
    pipelineDetail.setFromCoordinates(padPipeline.getFromCoordinates());
    pipelineDetail.setToLocation(padPipeline.getToLocation());
    pipelineDetail.setToCoordinates(padPipeline.getToCoordinates());
    pipelineDetail.setLength(padPipeline.getLength());
    return pipelineDetail;
  }

  public static PwaConsentApplicationDto createConsentApplicationDto(Instant consentInstant) {
    return new PwaConsentApplicationDto(
        1, consentInstant, "consent ref", 1, PwaApplicationType.INITIAL, "app ref");
  }



}
