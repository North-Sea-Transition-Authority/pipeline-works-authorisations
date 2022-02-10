package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;

/* Regardless of application or consented status, provide consistent interface for common pipeline data */
public interface PipelineEntity {

  Integer getId();

  void setId(Integer id);

  Pipeline getPipeline();

  void setPipeline(Pipeline pipeline);

  PipelineId getPipelineId();

  PipelineType getPipelineType();

  void setPipelineType(PipelineType pipelineType);

  String getFromLocation();

  void setFromLocation(String fromLocation);

  String getToLocation();

  void setToLocation(String toLocation);

  CoordinatePair getFromCoordinates();

  void setFromCoordinates(CoordinatePair fromCoordinates);

  CoordinatePair getToCoordinates();

  void setToCoordinates(CoordinatePair toCoordinates);

  String getComponentPartsDescription();

  void setComponentPartsDescription(String componentPartsDescription);

  BigDecimal getLength();

  void setLength(BigDecimal length);

  String getProductsToBeConveyed();

  void setProductsToBeConveyed(String productsToBeConveyed);

  Boolean getTrenchedBuriedBackfilled();

  void setTrenchedBuriedBackfilled(Boolean trenchedBuriedBackfilled);

  String getTrenchingMethodsDescription();

  void setTrenchingMethodsDescription(String trenchingMethodsDescription);

  PipelineFlexibility getPipelineFlexibility();

  void setPipelineFlexibility(PipelineFlexibility pipelineFlexibility);

  PipelineMaterial getPipelineMaterial();

  void setPipelineMaterial(PipelineMaterial pipelineMaterial);

  String getOtherPipelineMaterialUsed();

  void setOtherPipelineMaterialUsed(String otherPipelineMaterialUsed);

  Integer getPipelineDesignLife();

  void setPipelineDesignLife(Integer pipelineDesignLife);

  default PipelineCoreType getCoreType() {
    return getPipelineType().getCoreType();
  }

  BigDecimal getMaxExternalDiameter();

  void setMaxExternalDiameter(BigDecimal maxExternalDiameter);

  Boolean getPipelineInBundle();

  void setPipelineInBundle(Boolean pipelineInBundle);

  String getBundleName();

  void setBundleName(String bundleName);

  String getPipelineNumber();

  void setPipelineNumber(String pipelineNumber);

  PipelineStatus getPipelineStatus();

  void setPipelineStatus(PipelineStatus pipelineServiceStatus);

  String getPipelineStatusReason();

  void setPipelineStatusReason(String pipelineServiceStatusReason);

  String getFootnote();

  void setFootnote(String footnote);

}
