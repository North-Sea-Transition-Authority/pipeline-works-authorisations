package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines;

import java.math.BigDecimal;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadPipelineTestUtil {
  // no instantiation
  private PadPipelineTestUtil() {
  }

  // returns lowest level pad pipeline data with all parent relationships mapped up to Pipeline
  public static PadPipelineIdentData createPadPipeline(PwaApplicationDetail pwaApplicationDetail,
                                                       PipelineType pipelineType) throws IllegalAccessException {

    var pipeline = new Pipeline(pwaApplicationDetail.getPwaApplication());
    var padPipeline = createPadPipeline(pwaApplicationDetail, pipeline, pipelineType);
    var pipelineIdent = createPadPipelineident(padPipeline);
    return createPadPipelineIdentData(pipelineIdent);


  }

  public static PadPipeline createActivePadPipeline(PwaApplicationDetail pwaApplicationDetail,
                                              Pipeline pipeline) {
    var padPipeline = createPadPipeline(pwaApplicationDetail, pipeline);
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
    return padPipeline;
  }

  public static PadPipeline createInActivePadPipeline(PwaApplicationDetail pwaApplicationDetail,
                                                    Pipeline pipeline) {
    var padPipeline = createPadPipeline(pwaApplicationDetail, pipeline);
    padPipeline.setPipelineStatus(PipelineStatus.NEVER_LAID);
    return padPipeline;
  }

  private static PadPipeline createPadPipeline(PwaApplicationDetail pwaApplicationDetail,
                                               Pipeline pipeline) {
    try {
      return createPadPipeline(pwaApplicationDetail, pipeline, PipelineType.PRODUCTION_FLOWLINE);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Expected to be able to access field!", e.getCause());
    }
  }

  public static PadPipeline createPadPipeline(PwaApplicationDetail pwaApplicationDetail,
                                              Pipeline pipeline,
                                              PipelineType pipelineType) throws IllegalAccessException {
    var padPipeline = new PadPipeline();
    padPipeline.setPwaApplicationDetail(pwaApplicationDetail);
    padPipeline.setPipeline(pipeline);
    padPipeline.setMaxExternalDiameter(BigDecimal.TEN);
    padPipeline.setPipelineRef("PL001");
    padPipeline.setLength(BigDecimal.TEN);
    padPipeline.setProductsToBeConveyed("PROD");
    padPipeline.setComponentPartsDescription("PARTS");
    padPipeline.setToLocation("TO_LOCATION");
    padPipeline.setFromLocation("FROM_LOCATION");
    padPipeline.setPipelineInBundle(true);
    padPipeline.setBundleName("BUNDLE");
    padPipeline.setPipelineMaterial(PipelineMaterial.OTHER);
    padPipeline.setOtherPipelineMaterialUsed("OTHER");
    padPipeline.setPipelineDesignLife(100);
    padPipeline.setPipelineFlexibility(PipelineFlexibility.FLEXIBLE);
    padPipeline.setPipelineType(pipelineType);
    padPipeline.setTemporaryNumber(1);
    padPipeline.setTemporaryRef("TEMPORARY_REF");
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
    padPipeline.setPipelineStatusReason("REASON");
    padPipeline.setFromCoordinates(CoordinatePairTestUtil.getDefaultCoordinate(45, 0));
    padPipeline.setToCoordinates(CoordinatePairTestUtil.getDefaultCoordinate(46, 1));
    padPipeline.setTrenchedBuriedBackfilled(true);
    padPipeline.setTrenchingMethodsDescription("TRENCHING");
    padPipeline.setAlreadyExistsOnSeabed(true);
    padPipeline.setPipelineInUse(false);
    padPipeline.setFootnote("Footnote information");
    padPipeline.setPipelineTransferAgreed(true);


    ObjectTestUtils.assertAllFieldsNotNull(padPipeline, PadPipeline.class, Set.of(PadPipeline_.ID));

    return padPipeline;
  }



  public static PadPipelineIdent createPadPipelineident(PadPipeline padPipeline) throws IllegalAccessException {
    var ident = new PadPipelineIdent();

    ident.setPadPipeline(padPipeline);
    ident.setIdentNo(1);
    ident.setLength(BigDecimal.TEN);
    ident.setFromCoordinates(CoordinatePairTestUtil.getDefaultCoordinate(45, 0));
    ident.setToCoordinates(CoordinatePairTestUtil.getDefaultCoordinate(46, 1));
    ident.setFromLocation("FROM_LOCATION");
    ident.setToLocation("TO_LOCATION");
    ident.setDefiningStructure(false);

    ObjectTestUtils.assertAllFieldsNotNull(ident, PadPipelineIdent.class, Set.of(PadPipelineIdent_.ID));
    return ident;

  }

  public static PadPipelineIdentData createPadPipelineIdentData(PadPipelineIdent padPipelineIdent) {

    var identData = new PadPipelineIdentData(padPipelineIdent);
    if (PipelineCoreType.MULTI_CORE.equals(padPipelineIdent.getPipelineCoreType())) {
      identData.setExternalDiameterMultiCore("EXT");
      identData.setInternalDiameterMultiCore("INT");
      identData.setMaopMultiCore("MAOP");
      identData.setWallThicknessMultiCore("THICK");
      identData.setInsulationCoatingTypeMultiCore("INS MULTI");
      identData.setProductsToBeConveyedMultiCore("PROD MULTI");
    } else {
      identData.setExternalDiameter(BigDecimal.TEN);
      identData.setInternalDiameter(BigDecimal.ONE);
      identData.setMaop(BigDecimal.ZERO);
      identData.setWallThickness(BigDecimal.ONE);
      identData.setInsulationCoatingType("INS SINGLE");
      identData.setProductsToBeConveyed("PROD SINGLE");
    }

    identData.setComponentPartsDesc("PARTS");
    return identData;
  }
}
