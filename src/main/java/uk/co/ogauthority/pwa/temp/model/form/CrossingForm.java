package uk.co.ogauthority.pwa.temp.model.form;

import uk.co.ogauthority.pwa.temp.model.locations.CrossingType;

public class CrossingForm {

  private CrossingType crossingType;

  private String blockNumber;
  private Integer licenseNumber;
  private String companyAgreement;
  private String cableNameOrLocation;
  private String holderOfCable;
  private String pipelineNumber;
  private String ownerOfPipeline;

  public CrossingType getCrossingType() {
    return crossingType;
  }

  public void setCrossingType(CrossingType crossingType) {
    this.crossingType = crossingType;
  }

  public String getBlockNumber() {
    return blockNumber;
  }

  public void setBlockNumber(String blockNumber) {
    this.blockNumber = blockNumber;
  }

  public Integer getLicenseNumber() {
    return licenseNumber;
  }

  public void setLicenseNumber(Integer licenseNumber) {
    this.licenseNumber = licenseNumber;
  }

  public String getCompanyAgreement() {
    return companyAgreement;
  }

  public void setCompanyAgreement(String companyAgreement) {
    this.companyAgreement = companyAgreement;
  }

  public String getCableNameOrLocation() {
    return cableNameOrLocation;
  }

  public void setCableNameOrLocation(String cableNameOrLocation) {
    this.cableNameOrLocation = cableNameOrLocation;
  }

  public String getHolderOfCable() {
    return holderOfCable;
  }

  public void setHolderOfCable(String holderOfCable) {
    this.holderOfCable = holderOfCable;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public void setPipelineNumber(String pipelineNumber) {
    this.pipelineNumber = pipelineNumber;
  }

  public String getOwnerOfPipeline() {
    return ownerOfPipeline;
  }

  public void setOwnerOfPipeline(String ownerOfPipeline) {
    this.ownerOfPipeline = ownerOfPipeline;
  }
}
