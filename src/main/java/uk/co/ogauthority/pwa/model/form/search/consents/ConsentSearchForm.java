package uk.co.ogauthority.pwa.model.form.search.consents;

import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;

public class ConsentSearchForm {

  private Integer holderOuId;

  public static ConsentSearchForm fromSearchParams(ConsentSearchParams consentSearchParams) {

    var form = new ConsentSearchForm();
    form.setHolderOuId(consentSearchParams.getHolderOrgUnitId());
    return form;

  }

  public Integer getHolderOuId() {
    return holderOuId;
  }

  public void setHolderOuId(Integer holderOuId) {
    this.holderOuId = holderOuId;
  }

}
