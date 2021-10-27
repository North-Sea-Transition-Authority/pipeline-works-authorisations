package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

public class PadPipelineTaskListServiceTestUtil {


  private static final Logger LOGGER = LoggerFactory.getLogger(PadPipelineTaskListServiceTestUtil.class);

  private PadPipelineTaskListServiceTestUtil(){}




  public static PadPipeline createPadPipeline(PwaApplicationDetail pwaApplicationDetail, Pipeline pipeline) {
    PadPipeline padPipeline = new PadPipeline();
    try {
      padPipeline = PadPipelineTestUtil.createPadPipeline(pwaApplicationDetail, pipeline, PipelineType.PRODUCTION_FLOWLINE);
    } catch (IllegalAccessException e) {
      LOGGER.debug("Unable to create pad pipeline via the PadPipelineTestUtil");
    }
    return padPipeline;
  }

  public static PadPipelineIdent createIdentWithMatchingHeaderFromLocation(PadPipeline padPipeline) {
    PadPipelineIdent fromIdent = null;
    try {
      fromIdent = PadPipelineTestUtil.createPadPipelineident(padPipeline);
      fromIdent.setFromLocation(padPipeline.getFromLocation());
    } catch (IllegalAccessException e) {
      LOGGER.debug("Unable to create pad pipeline ident via the PadPipelineTestUtil");
    }
    return fromIdent;
  }

  public static PadPipelineIdent createIdentWithUnMatchingHeaderFromLocation(PadPipeline padPipeline) {
    PadPipelineIdent fromIdent = null;
    try {
      fromIdent = PadPipelineTestUtil.createPadPipelineident(padPipeline);
      fromIdent.setFromLocation(padPipeline.getFromLocation() + "xyz");
    } catch (IllegalAccessException e) {
      LOGGER.debug("Unable to create pad pipeline ident via the PadPipelineTestUtil");
    }
    return fromIdent;
  }

  public static PadPipelineIdent createIdentWithMatchingHeaderToLocation(PadPipeline padPipeline) {
    PadPipelineIdent toIdent = null;
    try {
      toIdent = PadPipelineTestUtil.createPadPipelineident(padPipeline);
      toIdent.setToLocation(padPipeline.getToLocation());
    } catch (IllegalAccessException e) {
      LOGGER.debug("Unable to create pad pipeline ident via the PadPipelineTestUtil");
    }
    return toIdent;
  }

  public static PadPipelineIdent createIdentWithUnMatchingHeaderToLocation(PadPipeline padPipeline) {
    PadPipelineIdent toIdent = null;
    try {
      toIdent = PadPipelineTestUtil.createPadPipelineident(padPipeline);
      toIdent.setToLocation(padPipeline.getToLocation() + "xyz");
    } catch (IllegalAccessException e) {
      LOGGER.debug("Unable to create pad pipeline ident via the PadPipelineTestUtil");
    }
    return toIdent;
  }

  public static CoordinatePair createCoordinatePairUnMatchingHeaderFromLocation(PadPipeline padPipeline) {
    var coordinatePair = CoordinatePairTestUtil.getDefaultCoordinate(
        padPipeline.getFromLatitudeDegrees() + 1, padPipeline.getFromLongDeg() + 1);
    var existingLatitudeDirection = coordinatePair.getLatitude().getDirection();
    var oppositeLatitudeDirection = existingLatitudeDirection == LatitudeDirection.NORTH ? LatitudeDirection.SOUTH : LatitudeDirection.NORTH;
    coordinatePair.getLatitude().setDirection(oppositeLatitudeDirection);
    return coordinatePair;
  }

  public static CoordinatePair createCoordinatePairUnMatchingHeaderFromLocationDirectionUnchanged(PadPipeline padPipeline) {
    var coordinatePair = CoordinatePairTestUtil.getDefaultCoordinate(
        padPipeline.getFromLatitudeDegrees() + 1, padPipeline.getFromLongDeg() + 1);
    return coordinatePair;
  }

  public static CoordinatePair createCoordinatePairUnMatchingHeaderToLocation(PadPipeline padPipeline) {
    var coordinatePair = CoordinatePairTestUtil.getDefaultCoordinate(
        padPipeline.getToLatDeg() + 1, padPipeline.getToLongDeg() + 1);
    var existingLongDirection = coordinatePair.getLongitude().getDirection();
    var oppositeLongDirection = existingLongDirection == LongitudeDirection.EAST ? LongitudeDirection.WEST : LongitudeDirection.EAST;
    coordinatePair.getLongitude().setDirection(oppositeLongDirection);
    return coordinatePair;
  }

  public static CoordinatePair createCoordinatePairUnMatchingHeaderToLocationDirectionUnchanged(PadPipeline padPipeline) {
    var coordinatePair = CoordinatePairTestUtil.getDefaultCoordinate(
        padPipeline.getToLatDeg() + 1, padPipeline.getToLongDeg() + 1);
    return coordinatePair;
  }








}
