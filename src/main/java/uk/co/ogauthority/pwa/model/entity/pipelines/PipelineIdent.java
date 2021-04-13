package uk.co.ogauthority.pwa.model.entity.pipelines;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/**
 * Provides accessors to common pipelineIdent attributes across application and consented models.
 */
public interface PipelineIdent {

  Integer getPipelineIdentId();

  PipelineId getPipelineId();

  int getIdentNo();

  String getFromLocation();

  String getToLocation();

  BigDecimal getLength();

  Boolean getIsDefiningStructure();

  CoordinatePair getFromCoordinates();

  CoordinatePair getToCoordinates();

  PipelineCoreType getPipelineCoreType();

  void setIdentNo(int identNo);

  void setFromLocation(String fromLocation);

  void setFromCoordinates(CoordinatePair fromCoordinates);

  void setToLocation(String toLocation);

  void setToCoordinates(CoordinatePair toCoordinates);

  void setLength(BigDecimal length);

  void setDefiningStructure(Boolean isDefiningStructure);

}
