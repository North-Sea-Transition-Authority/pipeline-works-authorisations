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
   * For when a summary with no diff info is required.
   */
  public List<ApplicationSectionSummary> summarise(PwaApplicationDetail pwaApplicationDetail) {
    return summariseAsDiff(pwaApplicationDetail, pwaApplicationDetail);
  }


  /**
   * For when a summary with diff info is required. Lopp over
   */
  @Transactional(readOnly = true)
  public List<ApplicationSectionSummary> summariseAsDiff(PwaApplicationDetail newPwaApplicationDetail,
                                                         PwaApplicationDetail oldPwaApplicationDetail) {

    var appSummarySections = new ArrayList<ApplicationSectionSummary>();
    ApplicationSectionSummaryType.getSummarySectionByProcessingOrder().forEach(applicationSectionSummaryType -> {
      var summariserService = springApplicationContext.getBean(
          applicationSectionSummaryType.getSectionSummariserServiceClass());

      if (summariserService.canSummarise(newPwaApplicationDetail, oldPwaApplicationDetail)) {

        appSummarySections.add(
            summariserService.summariseDifferences(
                newPwaApplicationDetail,
                oldPwaApplicationDetail,
                applicationSectionSummaryType.getTemplatePath())
        );

      }

    });

    return appSummarySections;

  }
}
