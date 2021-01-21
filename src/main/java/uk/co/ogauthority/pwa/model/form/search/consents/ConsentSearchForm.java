package uk.co.ogauthority.pwa.model.form.search.consents;

import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;

public class ConsentSearchForm {

  private Integer holderOrgUnitId;

  public static ConsentSearchForm fromSearchParams(ConsentSearchParams consentSearchParams) {

    var form = new ConsentSearchForm();
    form.setHolderOrgUnitId(consentSearchParams.getHolderOrgUnitId());
    return form;

  }

  public Integer getHolderOrgUnitId() {
    return holderOrgUnitId;
  }

  public void setHolderOrgUnitId(Integer holderOrgUnitId) {
    this.holderOrgUnitId = holderOrgUnitId;
  }

}
