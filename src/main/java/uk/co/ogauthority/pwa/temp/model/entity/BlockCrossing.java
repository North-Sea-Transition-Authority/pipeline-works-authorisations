package uk.co.ogauthority.pwa.temp.model.entity;

public class BlockCrossing {

  private String blockNumber;
  private Integer licenseNumber;
  private String operatorAgreement;

  public BlockCrossing(String blockNumber, Integer licenseNumber, String operatorAgreement) {
    this.blockNumber = blockNumber;
    this.licenseNumber = licenseNumber;
    this.operatorAgreement = operatorAgreement;
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

  public String getOperatorAgreement() {
    return operatorAgreement;
  }

  public void setOperatorAgreement(String operatorAgreement) {
    this.operatorAgreement = operatorAgreement;
  }
}
