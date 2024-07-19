package uk.co.ogauthority.pwa.model.entity.search.consents;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "vw_pwa_holder_org_units")
@Immutable
public class PwaHolderOrgUnit {

  @Id
  @Column(name = "composite_id")
  private String compositeId;

  private Integer pwaId;

  private Integer ouId;

  private String ouName;

  private Integer orgGrpId;

  private String migratedOrganisationName;

  public String getCompositeId() {
    return compositeId;
  }

  public void setCompositeId(String rowId) {
    this.compositeId = rowId;
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

  public String getMigratedOrganisationName() {
    return migratedOrganisationName;
  }

  public void setMigratedOrganisationName(String migratedOrganisationName) {
    this.migratedOrganisationName = migratedOrganisationName;
  }
}
