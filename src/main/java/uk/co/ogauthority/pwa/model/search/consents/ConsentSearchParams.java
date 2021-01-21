package uk.co.ogauthority.pwa.model.search.consents;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.form.search.consents.ConsentSearchForm;

public class ConsentSearchParams {

  // whether or not the search should be run
  private boolean search;

  private Integer holderOrgUnitId;

  public static ConsentSearchParams from(ConsentSearchForm form) {

    var params = new ConsentSearchParams();
    params.setHolderOrgUnitId(form.getHolderOuId());

    return params;

  }

  public boolean isSearch() {
    return search;
  }

  public void setSearch(boolean search) {
    this.search = search;
  }

  public Integer getHolderOrgUnitId() {
    return holderOrgUnitId;
  }

  public void setHolderOrgUnitId(Integer holderOrgUnitId) {
    this.holderOrgUnitId = holderOrgUnitId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentSearchParams that = (ConsentSearchParams) o;
    return search == that.search && Objects.equals(holderOrgUnitId, that.holderOrgUnitId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(search, holderOrgUnitId);
  }
}
