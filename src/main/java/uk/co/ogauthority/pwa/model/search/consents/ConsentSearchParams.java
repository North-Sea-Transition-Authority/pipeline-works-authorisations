package uk.co.ogauthority.pwa.model.search.consents;

import uk.co.ogauthority.pwa.model.form.search.consents.ConsentSearchForm;

public class ConsentSearchParams {

  // TODO PWA-1086 holder org grp manual filter
  private Integer holderOrgGroupId;

  public ConsentSearchParams() {
  }

  public static ConsentSearchParams from(ConsentSearchForm form) {

    var params = new ConsentSearchParams();

    return params;

  }

  public Integer getHolderOrgGroupId() {
    return holderOrgGroupId;
  }

  public void setHolderOrgGroupId(Integer holderOrgGroupId) {
    this.holderOrgGroupId = holderOrgGroupId;
  }

}
