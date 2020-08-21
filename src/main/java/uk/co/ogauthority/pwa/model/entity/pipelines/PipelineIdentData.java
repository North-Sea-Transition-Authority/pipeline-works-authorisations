package uk.co.ogauthority.pwa.model.entity.pipelines;

import java.math.BigDecimal;

/**
 * Provides accessors to common pipelineIdentData attributes across application and consented models.
 */
public interface PipelineIdentData {

  Integer getPipelineIdentDataId();

  PipelineIdent getPipelineIdent();

  String getComponentPartsDesc();

  BigDecimal getExternalDiameter();

  BigDecimal getInternalDiameter();

  BigDecimal getWallThickness();

  String getInsulationCoatingType();

  BigDecimal getMaop();

  String getProductsToBeConveyed();

  String getExternalDiameterMultiCore();

  String getInternalDiameterMultiCore();

  String getWallThicknessMultiCore();

  String getInsulationCoatingTypeMultiCore();

  String getMaopMultiCore();

  String getProductsToBeConveyedMultiCore();

}
