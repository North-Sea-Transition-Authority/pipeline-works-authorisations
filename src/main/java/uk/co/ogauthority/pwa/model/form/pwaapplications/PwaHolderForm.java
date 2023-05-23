package uk.co.ogauthority.pwa.model.form.pwaapplications;

import java.io.Serializable;

public class PwaHolderForm implements Serializable {

  private Integer holderOuId;

  public PwaHolderForm() {
  }

  public Integer getHolderOuId() {
    return holderOuId;
  }

  public void setHolderOuId(Integer holderOuId) {
    this.holderOuId = holderOuId;
  }

}
