package uk.co.ogauthority.pwa.model.entity.pipelines;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/**
 * Provides accessors to common pipelineIdent attributes across application and consented models.
 */
public interface PipelineIdent {

  Integer getPipelineIdentId();

  int getIdentNo();

  String getFromLocation();

  String getToLocation();

  BigDecimal getLength();

  CoordinatePair getFromCoordinates();

  CoordinatePair getToCoordinates();

  PipelineCoreType getPipelineCoreType();

}
