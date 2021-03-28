package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

public class PickPwaForm {
  private Integer consentedMasterPwaId;

  private Integer nonConsentedMasterPwaId;

  public Integer getConsentedMasterPwaId() {
    return consentedMasterPwaId;
  }

  public void setConsentedMasterPwaId(Integer consentedMasterPwaId) {
    this.consentedMasterPwaId = consentedMasterPwaId;
  }

  public Integer getNonConsentedMasterPwaId() {
    return nonConsentedMasterPwaId;
  }

  public void setNonConsentedMasterPwaId(Integer nonConsentedMasterPwaId) {
    this.nonConsentedMasterPwaId = nonConsentedMasterPwaId;
  }
}
