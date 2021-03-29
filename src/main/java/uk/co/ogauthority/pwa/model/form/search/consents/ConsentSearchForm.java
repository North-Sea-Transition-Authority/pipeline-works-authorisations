package uk.co.ogauthority.pwa.model.form.search.consents;

import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;

public class ConsentSearchForm {

  private Integer holderOuId;

  private String consentReference;

  private String pipelineReference;

  public static ConsentSearchForm fromSearchParams(ConsentSearchParams consentSearchParams) {

    var form = new ConsentSearchForm();
    form.setHolderOuId(consentSearchParams.getHolderOrgUnitId());
    form.setConsentReference(consentSearchParams.getConsentReference());
    form.setPipelineReference(consentSearchParams.getPipelineReference());
    return form;

  }

  public Integer getHolderOuId() {
    return holderOuId;
  }

  public void setHolderOuId(Integer holderOuId) {
    this.holderOuId = holderOuId;
  }

  public String getConsentReference() {
    return consentReference;
  }

  public void setConsentReference(String consentReference) {
    this.consentReference = consentReference;
  }

  public String getPipelineReference() {
    return pipelineReference;
  }

  public void setPipelineReference(String pipelineReference) {
    this.pipelineReference = pipelineReference;
  }
}
