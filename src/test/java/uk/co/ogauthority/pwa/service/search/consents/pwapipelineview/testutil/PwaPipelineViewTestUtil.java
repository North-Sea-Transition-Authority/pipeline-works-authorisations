package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.testutil;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummary;

public class PwaPipelineViewTestUtil {


  private static final Logger LOGGER = LoggerFactory.getLogger(PwaPipelineViewTestUtil.class);

  private PwaPipelineViewTestUtil(){}


  public static PipelineDiffableSummary createPipelineDiffableSummary(int pipelineId) {

    var pipelineDetail =  new PipelineDetail();
    IdentView identView = null;
    var pipeline = new Pipeline();
    pipeline.setId(pipelineId);

    try {
      PadPipeline padPipeline = PadPipelineTestUtil.createPadPipeline(new PwaApplicationDetail(), pipeline, PipelineType.PRODUCTION_FLOWLINE);
      pipelineDetail.setPipeline(pipeline);
      pipelineDetail.setPipelineNumber(padPipeline.getPipelineRef());
      pipelineDetail.setPipelineStatus(padPipeline.getPipelineStatus());
      pipelineDetail.setFromLocation(padPipeline.getFromLocation());
      pipelineDetail.setFromCoordinates(padPipeline.getFromCoordinates());
      pipelineDetail.setToLocation(padPipeline.getToLocation());
      pipelineDetail.setToCoordinates(padPipeline.getToCoordinates());
      pipelineDetail.setLength(padPipeline.getLength());
      pipelineDetail.setComponentPartsDescription(padPipeline.getComponentPartsDescription());
      pipelineDetail.setProductsToBeConveyed(padPipeline.getProductsToBeConveyed());
      pipelineDetail.setTrenchedBuriedBackfilled(padPipeline.getTrenchedBuriedBackfilled());
      pipelineDetail.setTrenchingMethodsDescription(padPipeline.getTrenchingMethodsDescription());
      pipelineDetail.setPipelineFlexibility(padPipeline.getPipelineFlexibility());
      pipelineDetail.setPipelineMaterial(padPipeline.getPipelineMaterial());
      pipelineDetail.setOtherPipelineMaterialUsed(padPipeline.getOtherPipelineMaterialUsed());

      PadPipelineIdent ident = PadPipelineTestUtil.createPadPipelineident(padPipeline);
      ident.setIdentNo(1);
      var identData = PadPipelineTestUtil.createPadPipelineIdentData(ident);
      identView = new IdentView(identData);

    } catch (IllegalAccessException e) {
      LOGGER.debug("Unable to create pad pipeline via the PadPipelineTestUtil");
    }


    return PipelineDiffableSummary.from(new PipelineHeaderView(pipelineDetail), List.of(identView), null);
  }


  public static PwaConsent createPwaConsent(String reference) {
    var pwaConsent = new PwaConsent();
    pwaConsent.setReference(reference);
    return pwaConsent;
  }




}
