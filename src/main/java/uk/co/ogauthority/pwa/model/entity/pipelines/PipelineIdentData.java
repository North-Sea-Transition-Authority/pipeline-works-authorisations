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

  void setComponentPartsDesc(String componentPartsDesc);

  void setExternalDiameter(BigDecimal externalDiameter);

  void setInternalDiameter(BigDecimal internalDiameter);

  void setWallThickness(BigDecimal wallThickness);

  void setInsulationCoatingType(String insulationCoatingType);

  void setMaop(BigDecimal maop);

  void setProductsToBeConveyed(String productsToBeConveyed);

  void setExternalDiameterMultiCore(String externalDiameterMultiCore);

  void setInternalDiameterMultiCore(String internalDiameterMultiCore);

  void setWallThicknessMultiCore(String wallThicknessMultiCore);

  void setInsulationCoatingTypeMultiCore(String insulationCoatingTypeMultiCore);

  void setMaopMultiCore(String maopMultiCore);

  void setProductsToBeConveyedMultiCore(String productsToBeConveyedMultiCore);
}
