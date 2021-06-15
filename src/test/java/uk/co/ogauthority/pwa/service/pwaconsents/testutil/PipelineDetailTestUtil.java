package uk.co.ogauthority.pwa.service.pwaconsents.testutil;

import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

public class PipelineDetailTestUtil {


  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineDetailTestUtil.class);

  private PipelineDetailTestUtil(){}


  public static PipelineDetail createPipelineDetail(Integer id, PipelineId pipelineId, Instant startTimestamp) {

    var pipeline = new Pipeline();
    pipeline.setId(pipelineId.asInt());
    var pipelineDetail = new PipelineDetail(pipeline);
    pipelineDetail.setId(id);
    pipelineDetail.setStartTimestamp(startTimestamp);
    pipelineDetail.setPwaConsent(new PwaConsent());
    pipelineDetail.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    pipelineDetail.setMaxExternalDiameter(BigDecimal.valueOf(100));
    pipelineDetail.setPipelineStatus(PipelineStatus.IN_SERVICE);
    return pipelineDetail;
  }

  public static PipelineDetail createPipelineDetail(Integer id, PipelineId pipelineId, Instant startTimestamp, PwaConsent pwaConsent) {
    var pipelineDetail = createPipelineDetail(id, pipelineId, startTimestamp);
    pipelineDetail.setPwaConsent(pwaConsent);
    return pipelineDetail;
  }

  public static PipelineDetail createPipelineDetail_withDefaultPipelineNumber(Integer id, PipelineId pipelineId, Instant startTimestamp) {
    var pipelineDetail = createPipelineDetail(id, pipelineId, startTimestamp);
    pipelineDetail.setPipelineNumber("PL6001");
    return pipelineDetail;
  }

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
      LOGGER.debug("Unable to create pad pipeline via the PadPipelineTestUtil");
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




}
