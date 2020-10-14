package uk.co.ogauthority.pwa.model.view.appsummary;

import java.util.List;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

public class ApplicationSummaryView {

  private String summaryHtml;
  private List<SidebarSectionLink> sidebarSectionLinks;

  public ApplicationSummaryView(String summaryHtml,
                                List<SidebarSectionLink> sidebarSectionLinks) {
    this.summaryHtml = summaryHtml;
    this.sidebarSectionLinks = sidebarSectionLinks;
  }

  public String getSummaryHtml() {
    return summaryHtml;
  }

  public void setSummaryHtml(String summaryHtml) {
    this.summaryHtml = summaryHtml;
  }

  public List<SidebarSectionLink> getSidebarSectionLinks() {
    return sidebarSectionLinks;
  }

  public void setSidebarSectionLinks(
      List<SidebarSectionLink> sidebarSectionLinks) {
    this.sidebarSectionLinks = sidebarSectionLinks;
  }

}
