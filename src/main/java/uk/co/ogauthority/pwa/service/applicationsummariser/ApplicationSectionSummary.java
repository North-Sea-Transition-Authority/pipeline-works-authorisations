package uk.co.ogauthority.pwa.service.applicationsummariser;

import java.util.List;
import java.util.Map;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;

/**
 * Encapsulates a single summarised section. Used By templates
 */
public final class ApplicationSectionSummary {

  private final List<SidebarSectionLink> sidebarSectionLinks;

  private final String templatePath;

  private final Map<String, Object> templateModel;

  public ApplicationSectionSummary(
      String templatePath,
      List<SidebarSectionLink> sidebarSectionLinks,
      Map<String, Object> templateModel) {
    this.sidebarSectionLinks = sidebarSectionLinks;
    this.templatePath = templatePath;
    this.templateModel = templateModel;
  }

  public List<SidebarSectionLink> getSidebarSectionLinks() {
    return sidebarSectionLinks;
  }

  public String getTemplatePath() {
    return templatePath;
  }

  public Map<String, Object> getTemplateModel() {
    return templateModel;
  }
}
