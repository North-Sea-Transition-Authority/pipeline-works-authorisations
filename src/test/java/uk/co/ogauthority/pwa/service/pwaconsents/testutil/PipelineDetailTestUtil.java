package uk.co.ogauthority.pwa.service.pwaconsents.testutil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineOverview;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;

public class PipelineDetailTestUtil {


  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineDetailTestUtil.class);

  private PipelineDetailTestUtil(){}


  public static PipelineDetail createPipelineDetail(Integer id, PipelineId pipelineId, Instant startTimestamp) {
    var masterPwa = MasterPwaTestUtil.create(100);
    var pipeline = new Pipeline();
    pipeline.setId(pipelineId.asInt());
    pipeline.setMasterPwa(masterPwa);
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
    return new PipelineHeaderView(pipelineDetail, null, null);
  }

  public static PipelineOverview createPipelineOverview(String referenceId, PipelineStatus pipelineStatus) {
    PadPipeline padPipeline = createPadPipeline();
    var pipelineDetail = createPipelineDetail(padPipeline, referenceId);
    pipelineDetail.setPipelineStatus(pipelineStatus);
    return new PipelineHeaderView(pipelineDetail, null, null);
  }

  public static PipelineOverview createPipelineOverviewWithAsBuiltStatus(String referenceId, PipelineStatus pipelineStatus,
                                                                          AsBuiltNotificationStatus asBuiltNotificationStatus) {
    PadPipeline padPipeline = createPadPipeline();
    var padPipelineOverView = new PadPipelineOverview(padPipeline, 1L);
    return PadPipelineOverview.from(padPipelineOverView, asBuiltNotificationStatus);
  }

  public static PipelineOverview createPipelineOverviewWithAsBuiltStatus(PipelineOverview pipelineOverview,
                                                                         AsBuiltNotificationStatus asBuiltNotificationStatus) {
    return PadPipelineOverview.from(pipelineOverview, asBuiltNotificationStatus);
  }

  private static PadPipeline createPadPipeline() {
    var pipeline = new Pipeline();
    pipeline.setId(Math.abs(new Random().nextInt()));
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
