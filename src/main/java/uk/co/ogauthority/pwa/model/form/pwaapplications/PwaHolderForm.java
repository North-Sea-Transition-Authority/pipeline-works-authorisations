package uk.co.ogauthority.pwa.model.form.pwaapplications;

import java.io.Serializable;
import uk.co.ogauthority.pwa.model.enums.PwaResourceType;

public class PwaHolderForm implements Serializable {

  private Integer holderOuId;

  private PwaResourceType resourceType;

  public PwaHolderForm() {
  }

  public Integer getHolderOuId() {
    return holderOuId;
  }

  public void setHolderOuId(Integer holderOuId) {
    this.holderOuId = holderOuId;
  }

  public PwaResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(PwaResourceType resourceType) {
    this.resourceType = resourceType;
  }
}
