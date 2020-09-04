package uk.co.ogauthority.pwa.service.applicationsummariser;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.ApplicationContactsSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.PermanentDepositSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.PipelinesSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.ProjectInformationSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.TechnicalDrawingsSummaryService;

/**
 * Defines basic details about application summary sections. Including:
 * - the order they should be processed when generating a whole app summary;
 * - the path to the template which can render the summary
 * - the service to be used to generate the summary itself.
 */
public enum ApplicationSectionSummaryType {

  APPLICATION_USERS(
      10,
      "pwaApplication/applicationSummarySections/applicationContactsSummary.ftl",
      ApplicationContactsSummaryService.class),
  PROJECT_INFORMATION(
      30,
      "pwaApplication/applicationSummarySections/projectInformationSummary.ftl",
      ProjectInformationSummaryService.class),
  PIPELINES(
      40,
      "pwaApplication/applicationSummarySections/pipelineSummary.ftl",
      PipelinesSummaryService.class),
  TECHNICAL_DRAWINGS(
      50,
      "pwaApplication/applicationSummarySections/technicalDrawingsSummary.ftl",
      TechnicalDrawingsSummaryService.class),
  PERMANENT_DEPOSIT(
      60,
      "pwaApplication/applicationSummarySections/permanentDepositsSummary.ftl",
      PermanentDepositSummaryService.class);

  private final int processingOrder;
  private final String templatePath;
  private final Class<? extends ApplicationSectionSummariser> sectionSummariserServiceClass;

  ApplicationSectionSummaryType(int processingOrder,
                                String templatePath,
                                Class<? extends ApplicationSectionSummariser> sectionSummariserServiceClass) {
    this.processingOrder = processingOrder;
    this.templatePath = templatePath;
    this.sectionSummariserServiceClass = sectionSummariserServiceClass;
  }

  public String getTemplatePath() {
    return templatePath;
  }

  public int getProcessingOrder() {
    return processingOrder;
  }

  public Class<? extends ApplicationSectionSummariser> getSectionSummariserServiceClass() {
    return sectionSummariserServiceClass;
  }

  public static List<ApplicationSectionSummaryType> getSummarySectionByProcessingOrder() {
    return Arrays.stream(ApplicationSectionSummaryType.values())
        .sorted(Comparator.comparing(ApplicationSectionSummaryType::getProcessingOrder))
        .collect(Collectors.toList());
  }
}
