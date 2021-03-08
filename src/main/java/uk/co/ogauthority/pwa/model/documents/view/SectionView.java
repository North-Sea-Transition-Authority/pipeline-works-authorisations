package uk.co.ogauthority.pwa.model.documents.view;

import java.util.ArrayList;
import java.util.List;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

public class SectionView {

  private String name;
  private int displayOrder;

  private List<SectionClauseVersionView> clauses;

  private List<SidebarSectionLink> sidebarSectionLinks;

  public SectionView() {
    this.clauses = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }

  public List<SectionClauseVersionView> getClauses() {
    return clauses;
  }

  public void setClauses(List<SectionClauseVersionView> clauses) {
    this.clauses = clauses;
  }

  public List<SidebarSectionLink> getSidebarSectionLinks() {
    return sidebarSectionLinks;
  }

  public void setSidebarSectionLinks(
      List<SidebarSectionLink> sidebarSectionLinks) {
    this.sidebarSectionLinks = sidebarSectionLinks;
  }
}
