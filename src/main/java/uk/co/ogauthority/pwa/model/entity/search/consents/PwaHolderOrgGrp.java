package uk.co.ogauthority.pwa.model.entity.search.consents;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "vw_pwa_holder_org_grps")
@Immutable
public class PwaHolderOrgGrp {

  @Id
  @Column(name = "row_num")
  private Integer rowId;

  private Integer pwaId;

  private Integer orgGrpId;

  public PwaHolderOrgGrp() {
  }

  @VisibleForTesting
  public PwaHolderOrgGrp(Integer rowId, Integer pwaId, Integer orgGrpId) {
    this.rowId = rowId;
    this.pwaId = pwaId;
    this.orgGrpId = orgGrpId;
  }

  public Integer getRowId() {
    return rowId;
  }

  public void setRowId(Integer rowId) {
    this.rowId = rowId;
  }

  public Integer getPwaId() {
    return pwaId;
  }

  public void setPwaId(Integer pwaId) {
    this.pwaId = pwaId;
  }

  public Integer getOrgGrpId() {
    return orgGrpId;
  }

  public void setOrgGrpId(Integer orgGrpId) {
    this.orgGrpId = orgGrpId;
  }

}
