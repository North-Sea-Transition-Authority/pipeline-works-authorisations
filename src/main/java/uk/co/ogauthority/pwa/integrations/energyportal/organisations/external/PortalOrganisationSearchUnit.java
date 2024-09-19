package uk.co.ogauthority.pwa.integrations.energyportal.organisations.external;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;

@Entity
@Table(name = "vw_portal_org_unit_searchable")
@Immutable
public class PortalOrganisationSearchUnit implements SearchSelectable {

  @Id
  @Column(name = "org_unit_id")
  private int orgUnitId;

  @Column(name = "org_search_name")
  private String orgSearchableUnitName;

  @Column(name = "org_grp_id")
  private Integer groupId;

  private boolean isActive;

  public int getOrgUnitId() {
    return orgUnitId;
  }

  public void setOrgUnitId(int data) {
    this.orgUnitId = data;
  }

  public String getOrgSearchableUnitName() {
    return orgSearchableUnitName;
  }

  public void setOrgSearchableUnitName(String key) {
    this.orgSearchableUnitName = key;
  }

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  @Override
  public String getSelectionId() {
    return String.valueOf(orgUnitId);
  }

  @Override
  public String getSelectionText() {
    return getOrgSearchableUnitName();
  }

  public PortalOrganisationSearchUnit() {

  }

  public PortalOrganisationSearchUnit(int orgUnitId, String orgSearchableUnitName, Integer groupId, boolean isActive) {
    this.orgUnitId = orgUnitId;
    this.orgSearchableUnitName = orgSearchableUnitName;
    this.groupId = groupId;
    this.isActive = isActive;
  }
}
