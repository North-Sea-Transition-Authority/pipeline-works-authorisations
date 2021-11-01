package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents;

import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

public class PipelineIdentDataForm {

  private String componentPartsDescription;
  private DecimalInput externalDiameter;
  private DecimalInput internalDiameter;
  private DecimalInput wallThickness;
  private String insulationCoatingType;
  private DecimalInput maop;
  private String productsToBeConveyed;

  private String externalDiameterMultiCore;
  private String internalDiameterMultiCore;
  private String wallThicknessMultiCore;
  private String insulationCoatingTypeMultiCore;
  private String maopMultiCore;
  private String productsToBeConveyedMultiCore;


  public PipelineIdentDataForm() {
  }

  public String getComponentPartsDescription() {
    return componentPartsDescription;
  }

  public void setComponentPartsDescription(String componentPartsDescription) {
    this.componentPartsDescription = componentPartsDescription;
  }

  public DecimalInput getExternalDiameter() {
    return externalDiameter;
  }

  public void setExternalDiameter(DecimalInput externalDiameter) {
    this.externalDiameter = externalDiameter;
  }

  public DecimalInput getInternalDiameter() {
    return internalDiameter;
  }

  public void setInternalDiameter(DecimalInput internalDiameter) {
    this.internalDiameter = internalDiameter;
  }

  public DecimalInput getWallThickness() {
    return wallThickness;
  }

  public void setWallThickness(DecimalInput wallThickness) {
    this.wallThickness = wallThickness;
  }

  public String getInsulationCoatingType() {
    return insulationCoatingType;
  }

  public void setInsulationCoatingType(String insulationCoatingType) {
    this.insulationCoatingType = insulationCoatingType;
  }

  public DecimalInput getMaop() {
    return maop;
  }

  public void setMaop(DecimalInput maop) {
    this.maop = maop;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }


  public String getExternalDiameterMultiCore() {
    return externalDiameterMultiCore;
  }

  public void setExternalDiameterMultiCore(String externalDiameterMultiCore) {
    this.externalDiameterMultiCore = externalDiameterMultiCore;
  }

  public String getInternalDiameterMultiCore() {
    return internalDiameterMultiCore;
  }

  public void setInternalDiameterMultiCore(String internalDiameterMultiCore) {
    this.internalDiameterMultiCore = internalDiameterMultiCore;
  }

  public String getWallThicknessMultiCore() {
    return wallThicknessMultiCore;
  }

  public void setWallThicknessMultiCore(String wallThicknessMultiCore) {
    this.wallThicknessMultiCore = wallThicknessMultiCore;
  }

  public String getInsulationCoatingTypeMultiCore() {
    return insulationCoatingTypeMultiCore;
  }

  public void setInsulationCoatingTypeMultiCore(String insulationCoatingTypeMultiCore) {
    this.insulationCoatingTypeMultiCore = insulationCoatingTypeMultiCore;
  }

  public String getMaopMultiCore() {
    return maopMultiCore;
  }

  public void setMaopMultiCore(String maopMultiCore) {
    this.maopMultiCore = maopMultiCore;
  }

  public String getProductsToBeConveyedMultiCore() {
    return productsToBeConveyedMultiCore;
  }

  public void setProductsToBeConveyedMultiCore(String productsToBeConveyedMultiCore) {
    this.productsToBeConveyedMultiCore = productsToBeConveyedMultiCore;
  }

}
