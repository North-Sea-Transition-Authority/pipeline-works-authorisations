package uk.co.ogauthority.pwa.service.applicationsummariser;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Processes a PwaApplicationDetail into a form suitable for rendering on screen.
 */
@Service
public class ApplicationSummaryService {

  private final ApplicationContext springApplicationContext;

  @Autowired
  public ApplicationSummaryService(ApplicationContext springApplicationContext) {
    this.springApplicationContext = springApplicationContext;
  }

  /**
   * Loop over each possible summary section, determine if it is appropriate for the application then generate the
   * list of ApplicationSectionSummary objects.
   */
  @Transactional(readOnly = true) // just a hint, not guaranteed to be enforced read only.
  public List<ApplicationSectionSummary> summarise(PwaApplicationDetail pwaApplicationDetail) {

    var appSummarySections = new ArrayList<ApplicationSectionSummary>();
    ApplicationSectionSummaryType.getSummarySectionByProcessingOrder().forEach(applicationSectionSummaryType -> {
      var summariserService = springApplicationContext.getBean(
          applicationSectionSummaryType.getSectionSummariserServiceClass());

      if (summariserService.canSummarise(pwaApplicationDetail)) {

        appSummarySections.add(
            summariserService.summariseSection(
                pwaApplicationDetail,
                applicationSectionSummaryType.getTemplatePath())
        );

      }

    });

    return appSummarySections;

  }
}
