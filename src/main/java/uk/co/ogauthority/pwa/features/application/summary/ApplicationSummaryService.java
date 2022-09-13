package uk.co.ogauthority.pwa.features.application.summary;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSummaryService.class);

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

      var summariserService = springApplicationContext
          .getBean(applicationSectionSummaryType.getSectionSummariserServiceClass());

      if (summariserService.canSummarise(pwaApplicationDetail)) {

        LOGGER.debug(String.format("Summarise started for app detail id %s and section %s",
            pwaApplicationDetail.getId(),
            applicationSectionSummaryType.getSectionSummariserServiceClass().getSimpleName()));

        appSummarySections.add(
            summariserService.summariseSection(
                pwaApplicationDetail,
                applicationSectionSummaryType.getTemplatePath())
        );

        LOGGER.debug(String.format("Summarise finished for app detail id %s and section %s",
            pwaApplicationDetail.getId(),
            applicationSectionSummaryType.getSectionSummariserServiceClass().getSimpleName()));

      }

    });

    return appSummarySections;

  }
}
