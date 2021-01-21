package uk.co.ogauthority.pwa.model.entity.search.consents;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "vw_pwa_holder_org_units")
@Immutable
public class PwaHolderOrgUnit {

  @Id
  @Column(name = "row_num")
  private Integer rowId;

  private Integer pwaId;

  private Integer ouId;

  private String ouName;

  private Integer orgGrpId;

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

  public Integer getOuId() {
    return ouId;
  }

  public void setOuId(Integer ouId) {
    this.ouId = ouId;
  }

  public String getOuName() {
    return ouName;
  }

  public void setOuName(String ouName) {
    this.ouName = ouName;
  }

  public Integer getOrgGrpId() {
    return orgGrpId;
  }

  public void setOrgGrpId(Integer orgGrpId) {
    this.orgGrpId = orgGrpId;
  }

}
