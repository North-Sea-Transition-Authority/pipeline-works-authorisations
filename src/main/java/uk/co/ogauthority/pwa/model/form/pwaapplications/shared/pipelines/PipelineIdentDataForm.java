package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines;

import java.math.BigDecimal;

public class PipelineIdentDataForm {

  private String componentPartsDescription;
  private BigDecimal externalDiameter;
  private BigDecimal internalDiameter;
  private BigDecimal wallThickness;
  private String insulationCoatingType;
  private BigDecimal maop;
  private String productsToBeConveyed;

  private String externalDiameterTxt;
  private String internalDiameterTxt;
  private String wallThicknessTxt;
  private String insulationCoatingTypeTxt;
  private String maopTxt;
  private String productsToBeConveyedTxt;


  public PipelineIdentDataForm() {
  }

  public String getComponentPartsDescription() {
    return componentPartsDescription;
  }

  public void setComponentPartsDescription(String componentPartsDescription) {
    this.componentPartsDescription = componentPartsDescription;
  }

  public BigDecimal getExternalDiameter() {
    return externalDiameter;
  }

  public void setExternalDiameter(BigDecimal externalDiameter) {
    this.externalDiameter = externalDiameter;
  }

  public BigDecimal getInternalDiameter() {
    return internalDiameter;
  }

  public void setInternalDiameter(BigDecimal internalDiameter) {
    this.internalDiameter = internalDiameter;
  }

  public BigDecimal getWallThickness() {
    return wallThickness;
  }

  public void setWallThickness(BigDecimal wallThickness) {
    this.wallThickness = wallThickness;
  }

  public String getInsulationCoatingType() {
    return insulationCoatingType;
  }

  public void setInsulationCoatingType(String insulationCoatingType) {
    this.insulationCoatingType = insulationCoatingType;
  }

  public BigDecimal getMaop() {
    return maop;
  }

  public void setMaop(BigDecimal maop) {
    this.maop = maop;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }


  public String getExternalDiameterTxt() {
    return externalDiameterTxt;
  }

  public void setExternalDiameterTxt(String externalDiameterTxt) {
    this.externalDiameterTxt = externalDiameterTxt;
  }

  public String getInternalDiameterTxt() {
    return internalDiameterTxt;
  }

  public void setInternalDiameterTxt(String internalDiameterTxt) {
    this.internalDiameterTxt = internalDiameterTxt;
  }

  public String getWallThicknessTxt() {
    return wallThicknessTxt;
  }

  public void setWallThicknessTxt(String wallThicknessTxt) {
    this.wallThicknessTxt = wallThicknessTxt;
  }

  public String getInsulationCoatingTypeTxt() {
    return insulationCoatingTypeTxt;
  }

  public void setInsulationCoatingTypeTxt(String insulationCoatingTypeTxt) {
    this.insulationCoatingTypeTxt = insulationCoatingTypeTxt;
  }

  public String getMaopTxt() {
    return maopTxt;
  }

  public void setMaopTxt(String maopTxt) {
    this.maopTxt = maopTxt;
  }

  public String getProductsToBeConveyedTxt() {
    return productsToBeConveyedTxt;
  }

  public void setProductsToBeConveyedTxt(String productsToBeConveyedTxt) {
    this.productsToBeConveyedTxt = productsToBeConveyedTxt;
  }

}
