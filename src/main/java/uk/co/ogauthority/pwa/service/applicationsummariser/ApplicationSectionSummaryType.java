package uk.co.ogauthority.pwa.service.applicationsummariser;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.ApplicationContactsSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.CableCrossingsSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.CampaignWorkScheduleSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.CrossingTypesSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.DepositDrawingsSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.DesignOpConditionsSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.EnvironmentalDecomSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.FastTrackSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.FieldInformationSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.FluidCompositionSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.GeneralTechInfoSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.LicenceBlockSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.LocationDetailsSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.MedianLineAgreementSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.OptionConfirmationSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.OptionsTemplateSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.OtherPropertiesSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.PartnerApprovalLettersSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.PermanentDepositSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.PipelineCrossingsSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.PipelinesSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.ProjectInformationSummaryService;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.SupplementaryDocumentsSummaryService;
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

  OPTION_CONFIRMATION(
      15,
      "pwaApplication/applicationSummarySections/optionConfirmationSummary.ftl",
      OptionConfirmationSummaryService.class),

  FIELD_INFORMATION(
      20,
      "pwaApplication/applicationSummarySections/fieldInformationSummary.ftl",
      FieldInformationSummaryService.class),

  PROJECT_INFORMATION(
      30,
      "pwaApplication/applicationSummarySections/projectInformationSummary.ftl",
      ProjectInformationSummaryService.class),

  OPTIONS_TEMPLATE(
      35,
      "pwaApplication/applicationSummarySections/optionsTemplateSummary.ftl",
      OptionsTemplateSummaryService.class),

  FAST_TRACK(
      40,
      "pwaApplication/applicationSummarySections/fastTrackSummary.ftl",
      FastTrackSummaryService.class
  ),

  SUPPLEMENTARY_DOCUMENTS(
      45,
      "pwaApplication/applicationSummarySections/supplementaryDocumentsSummary.ftl",
      SupplementaryDocumentsSummaryService.class),

  ENVIRONMENTAL_DECOMMISSIONING(
      50,
          "pwaApplication/applicationSummarySections/environmentalAndDecommissioningSummary.ftl",
      EnvironmentalDecomSummaryService.class),

  HUOO(
      60,
      "pwaApplication/applicationSummarySections/huooSummary.ftl",
      HuooSummaryService.class),

  PARTNER_LETTERS(
      70,
          "pwaApplication/applicationSummarySections/partnerApprovalLettersSummary.ftl",
      PartnerApprovalLettersSummaryService.class),

  LOCATION_DETAILS(
      80,
      "pwaApplication/applicationSummarySections/locationDetailsSummary.ftl",
      LocationDetailsSummaryService.class),

  LICENCE_AND_BLOCKS(
      90,
      "pwaApplication/applicationSummarySections/licenceBlockSummary.ftl",
      LicenceBlockSummaryService.class),

  CROSSING_TYPES(
      100,
      "pwaApplication/applicationSummarySections/crossingTypesSummary.ftl",
      CrossingTypesSummaryService.class),

  PIPELINE_CROSSINGS(
      110,
      "pwaApplication/applicationSummarySections/pipelineCrossingsSummary.ftl",
      PipelineCrossingsSummaryService.class),

  CABLE_CROSSINGS(
      120,
      "pwaApplication/applicationSummarySections/cableCrossingsSummary.ftl",
      CableCrossingsSummaryService.class),

  MEDIAN_LINE_CROSSING(
      130,
      "pwaApplication/applicationSummarySections/medianLineAgreementSummary.ftl",
      MedianLineAgreementSummaryService.class),

  GENERAL_TECH_DETAILS(
      140,
      "pwaApplication/applicationSummarySections/generalTechInfoSummary.ftl",
      GeneralTechInfoSummaryService.class),

  FLUID_COMPOSITION(
      150,
      "pwaApplication/applicationSummarySections/fluidCompositionSummary.ftl",
      FluidCompositionSummaryService.class),

  PIPELINE_OTHER_PROPERTIES(
      160,
      "pwaApplication/applicationSummarySections/otherPropertiesSummary.ftl",
      OtherPropertiesSummaryService.class),

  DESIGN_OP_CONDITIONS(
      170,
      "pwaApplication/applicationSummarySections/designOpConditionsSummary.ftl",
      DesignOpConditionsSummaryService.class
  ),

  PIPELINES(
      180,
      "pwaApplication/applicationSummarySections/pipelineSummary.ftl",
      PipelinesSummaryService.class),

  TECHNICAL_DRAWINGS(
      190,
      "pwaApplication/applicationSummarySections/technicalDrawingsSummary.ftl",
      TechnicalDrawingsSummaryService.class),

  CAMPAIGN_WORK_SCHEDULE(
      200,
      "pwaApplication/applicationSummarySections/campaignWorksSummary.ftl",
      CampaignWorkScheduleSummaryService.class),

  PERMANENT_DEPOSIT(
      210,
      "pwaApplication/applicationSummarySections/permanentDepositsSummary.ftl",
      PermanentDepositSummaryService.class),

  PERMANENT_DEPOSIT_DRAWINGS(
      220,
      "pwaApplication/applicationSummarySections/depositDrawingsSummary.ftl",
      DepositDrawingsSummaryService.class);


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
