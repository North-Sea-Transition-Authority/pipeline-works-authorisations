package uk.co.ogauthority.pwa.service.applicationsummariser;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;

@Service
public class ApplicationSummaryViewService {

  private final ApplicationSummaryService applicationSummaryService;
  private final TemplateRenderingService templateRenderingService;

  @Autowired
  public ApplicationSummaryViewService(ApplicationSummaryService applicationSummaryService,
                                       TemplateRenderingService templateRenderingService) {
    this.applicationSummaryService = applicationSummaryService;
    this.templateRenderingService = templateRenderingService;
  }

  public ApplicationSummaryView getApplicationSummaryView(PwaApplicationDetail pwaApplicationDetail) {

    var summarisedSections = applicationSummaryService.summarise(pwaApplicationDetail);

    String combinedRenderedSummaryHtml = summarisedSections.stream()
        .map(summary -> templateRenderingService.render(summary.getTemplatePath(), summary.getTemplateModel(), true))
        .collect(Collectors.joining());

    List<SidebarSectionLink> sidebarSectionLinks = summarisedSections.stream()
        .flatMap(o -> o.getSidebarSectionLinks().stream())
        .collect(Collectors.toList());

    return new ApplicationSummaryView(combinedRenderedSummaryHtml, sidebarSectionLinks);

  }

}
