package uk.co.ogauthority.pwa.service.pwaconsents.testutil;

import java.math.BigDecimal;
import java.time.Instant;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

public class PipelineDetailTestUtil {



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





}
