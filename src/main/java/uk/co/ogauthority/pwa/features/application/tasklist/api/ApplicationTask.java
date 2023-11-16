package uk.co.ogauthority.pwa.features.application.tasklist.api;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.tasks.appcontacts.controller.PwaContactController;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.controller.CampaignWorksController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.controller.CrossingAgreementsController;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditionsService;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.controller.DesignOpConditionsController;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.controller.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrackService;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.controller.FastTrackController;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadFieldService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.controller.PadPwaFieldsController;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfoService;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.controller.FluidCompositionInfoController;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfoService;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.controller.PipelineTechInfoController;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadHuooTaskSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.controller.HuooController;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetailsService;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.controller.LocationDetailsController;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.controller.ConfirmationOfOptionController;
import uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.OptionsTemplateService;
import uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.controller.OptionsTemplateController;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PadPipelineOtherPropertiesService;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.controller.PipelineOtherPropertiesController;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PadPartnerLettersService;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.controller.PartnerLettersController;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingsService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.controller.PermanentDepositController;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.controller.PermanentDepositDrawingsController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview.TechnicalDrawingSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview.controller.TechnicalDrawingsController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.controller.PipelinesHuooController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.controller.PipelinesController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.PadPipelineTaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.controller.PipelinesTaskListController;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.PadProjectExtensionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.controller.PadProjectExtensionController;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.controller.ProjectInformationController;
import uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs.SupplementaryDocumentsService;
import uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs.controller.SupplementaryDocumentsController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

/**
 * Enumeration of all app form tasks for the PWA application task list.
 */
public enum ApplicationTask implements GeneralPurposeApplicationTask {

  FIELD_INFORMATION(
      "Field information",
      PadPwaFieldsController.class,
      PadFieldService.class,
      1, 1
  ),

  CARBON_STORAGE_INFORMATION(
      "Carbon storage information",
      PadPwaFieldsController.class,
      PadFieldService.class,
      1, 1
  ),

  APPLICATION_USERS(
      "Application users",
      PwaContactController.class,
      PwaContactService.class,
      5, 5
  ),

  PROJECT_INFORMATION(
      "Project information",
      ProjectInformationController.class,
      PadProjectInformationService.class,
      10, 10
  ),

  OPTIONS_TEMPLATE(
      "Options template",
      OptionsTemplateController.class,
      OptionsTemplateService.class,
      15, 15
  ),

  CONFIRM_OPTIONS(
      "Confirm completed option",
      ConfirmationOfOptionController.class,
      PadConfirmationOfOptionService.class,
      16, 16
  ),

  SUPPLEMENTARY_DOCUMENTS(
      "Supplementary documents",
      SupplementaryDocumentsController.class,
      SupplementaryDocumentsService.class,
      18, 18
  ),

  FAST_TRACK(
      "Fast-track",
      FastTrackController.class,
      PadFastTrackService.class,
      20, 20
  ),

  PROJECT_EXTENSION(
      "Project timeline extension",
      PadProjectExtensionController.class,
      PadProjectExtensionService.class,
      25,
      25
  ),

  LOCATION_DETAILS(
      "Location details",
      LocationDetailsController.class,
      PadLocationDetailsService.class,
      30, 30
  ),

  ENVIRONMENTAL_DECOMMISSIONING(
      "Environmental and decommissioning",
      EnvironmentalDecomController.class,
      PadEnvironmentalDecommissioningService.class,
      40, 40
  ),

  HUOO(
      "Holders, users, operators, and owners",
      HuooController.class,
      PadHuooTaskSectionService.class,
      50, 50
  ),

  CROSSING_AGREEMENTS(
      "Blocks and crossing agreements",
      CrossingAgreementsController.class,
      CrossingAgreementsService.class,
      60, 60
  ),

  PIPELINES(
      "Pipelines",
      PipelinesController.class,
      PadPipelineTaskListService.class,
      70, 70
  ),

  PIPELINES_HUOO(
      "Pipeline holders, users, operators and owners",
      "Pipeline HUOOs",
      PipelinesHuooController.class,
      PadPipelinesHuooService.class,
      75, 75
  ),

  CAMPAIGN_WORKS(
      "Campaign works",
      CampaignWorksController.class,
      CampaignWorksService.class,
      80, 80
  ),

  TECHNICAL_DRAWINGS(
      "Pipeline schematics and other diagrams",
      TechnicalDrawingsController.class,
      TechnicalDrawingSectionService.class,
      90, 90
  ),

  PERMANENT_DEPOSITS(
      "Permanent deposits",
      PermanentDepositController.class,
      PermanentDepositService.class,
      100, 100
  ),

  PERMANENT_DEPOSIT_DRAWINGS(
      "Permanent deposit drawings",
      PermanentDepositDrawingsController.class,
      DepositDrawingsService.class,
      110, 110
  ),

  GENERAL_TECH_DETAILS(
      "General technical details",
      PipelineTechInfoController.class,
      PadPipelineTechInfoService.class,
      120, 120
  ),

  FLUID_COMPOSITION(
      "Fluid composition",
      FluidCompositionInfoController.class,
      PadFluidCompositionInfoService.class,
      130, 130
  ),

  PIPELINE_OTHER_PROPERTIES(
      "Other properties",
      PipelineOtherPropertiesController.class,
      PadPipelineOtherPropertiesService.class,
      140, 140
  ),

  DESIGN_OP_CONDITIONS(
      "Design and operating conditions",
      DesignOpConditionsController.class,
      PadDesignOpConditionsService.class,
      150, 150
  ),

  PARTNER_LETTERS(
      "Partner approval letters",
      PartnerLettersController.class,
      PadPartnerLettersService.class,
      160, 160
  );

  private final String displayName;
  private final String shortenedDisplayName;
  private final Class<?> controllerClass;
  private final Class<? extends ApplicationFormSectionService> serviceClass;
  private final int displayOrder;
  private final int versioningProcessingOrder;

  ApplicationTask(String displayName, String shortenedDisplayName, Class<?> controllerClass,
                  Class<? extends ApplicationFormSectionService> serviceClass,
                  int displayOrder, int versioningProcessingOrder) {
    this.displayName = displayName;
    this.shortenedDisplayName = shortenedDisplayName;
    this.controllerClass = controllerClass;
    this.serviceClass = serviceClass;
    this.displayOrder = displayOrder;
    this.versioningProcessingOrder = versioningProcessingOrder;
  }

  ApplicationTask(String displayName, Class<?> controllerClass,
                  Class<? extends ApplicationFormSectionService> serviceClass,
                  int displayOrder, int versioningProcessingOrder) {
    this(displayName, displayName, controllerClass, serviceClass, displayOrder, versioningProcessingOrder);
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String getShortenedDisplayName() {
    return shortenedDisplayName;
  }

  @Override
  public Class<?> getControllerClass() {
    return controllerClass;
  }

  @Override
  public Class<? extends ApplicationFormSectionService> getServiceClass() {
    return serviceClass;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public int getVersioningProcessingOrder() {
    return versioningProcessingOrder;
  }

  @Override
  public String getTaskLandingPageRoute(PwaApplication pwaApplication) {
    var applicationId = pwaApplication.getId();
    var applicationType = pwaApplication.getApplicationType();
    Map<String, Object> uriVariables = new HashMap<>();
    uriVariables.put("applicationId", applicationId);
    switch (this) {
      case FIELD_INFORMATION:
        return ReverseRouter.route(on(PadPwaFieldsController.class)
            .renderFields(applicationType, applicationId, null, null, null));
      case CARBON_STORAGE_INFORMATION:
        return ReverseRouter.route(on(PadPwaFieldsController.class)
            .renderFields(applicationType, applicationId, null, null, null));
      case APPLICATION_USERS:
        return ReverseRouter.route(on(PwaContactController.class)
            .renderContactsScreen(applicationType, applicationId, null, null));
      case PROJECT_INFORMATION:
        return ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(applicationType, applicationId, null, null));
      case FAST_TRACK:
        return ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(applicationType, applicationId, null, null, null));
      case PROJECT_EXTENSION:
        return ReverseRouter.route(on(PadProjectExtensionController.class)
            .renderProjectExtension(null, applicationId, applicationType, null));
      case OPTIONS_TEMPLATE:
        return ReverseRouter.route(on(OptionsTemplateController.class)
            .renderOptionsTemplate(applicationId, applicationType, null, null, null));
      case CONFIRM_OPTIONS:
        return ReverseRouter.route(on(ConfirmationOfOptionController.class)
            .renderConfirmOption(applicationType, applicationId, null, null));
      case SUPPLEMENTARY_DOCUMENTS:
        return ReverseRouter.route(on(SupplementaryDocumentsController.class)
            .renderSupplementaryDocuments(applicationId, applicationType, null, null, null));
      case ENVIRONMENTAL_DECOMMISSIONING:
        return ReverseRouter.route(on(EnvironmentalDecomController.class)
            .renderEnvDecom(applicationType, null, null), uriVariables);
      case CROSSING_AGREEMENTS:
        return ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(applicationType, applicationId, null, null));
      case LOCATION_DETAILS:
        return ReverseRouter.route(on(LocationDetailsController.class)
            .renderLocationDetails(applicationType, null, null, null), Map.of("applicationId", applicationId));
      case HUOO:
        return ReverseRouter.route(on(HuooController.class)
            .renderHuooSummary(applicationType, applicationId, null, null));
      case PIPELINES:
        return ReverseRouter.route(on(PipelinesTaskListController.class)
            .renderPipelinesOverview(applicationId, applicationType, null, null));
      case PIPELINES_HUOO:
        return ReverseRouter.route(on(PipelinesHuooController.class)
            .renderSummary(applicationType, applicationId, null));
      case CAMPAIGN_WORKS:
        return ReverseRouter.route(on(CampaignWorksController.class)
            .renderSummary(applicationType, applicationId, null));
      case TECHNICAL_DRAWINGS:
        return ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(applicationType, applicationId, null, null));
      case PERMANENT_DEPOSITS:
        return ReverseRouter.route(on(PermanentDepositController.class)
            .renderPermanentDepositsOverview(applicationType, applicationId, null, null));
      case PERMANENT_DEPOSIT_DRAWINGS:
        return ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .renderDepositDrawingsOverview(applicationType, applicationId, null, null));
      case GENERAL_TECH_DETAILS:
        return ReverseRouter.route(on(PipelineTechInfoController.class)
            .renderAddPipelineTechInfo(applicationType, applicationId, null, null));
      case FLUID_COMPOSITION:
        return ReverseRouter.route(on(FluidCompositionInfoController.class)
            .renderAddFluidCompositionInfo(applicationType, applicationId, null, null));
      case PIPELINE_OTHER_PROPERTIES:
        return ReverseRouter.route(on(PipelineOtherPropertiesController.class)
            .renderAddPipelineOtherProperties(applicationType, applicationId, null, null));
      case DESIGN_OP_CONDITIONS:
        return ReverseRouter.route(on(DesignOpConditionsController.class)
            .renderAddDesignOpConditions(applicationType, applicationId, null, null));
      case PARTNER_LETTERS:
        return ReverseRouter.route(on(PartnerLettersController.class)
            .renderAddPartnerLetters(applicationType, applicationId, null, null));
      default:
        return "";
    }
  }

  public static Stream<ApplicationTask> stream() {
    return Stream.of(ApplicationTask.values());
  }

  public static ApplicationTask resolveFromName(String displayName) {
    return ApplicationTask.stream()
        .filter(task -> Objects.equals(task.getDisplayName(), displayName))
        .findFirst()
        .orElseThrow(
            () -> new ValueNotFoundException(String.format("Couldn't find task with display name [%s]", displayName)));
  }

}
