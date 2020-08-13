package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.masterpwas.contacts.PwaContactController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.fields.PadPwaFieldsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.FastTrackController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.HuooController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.LocationDetailsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.campaignworks.CampaignWorksController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CrossingAgreementsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.partnerletters.PartnerLettersController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositDrawingsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PipelinesHuooController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinetechinfo.FluidCompositionInfoController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.TechnicalDrawingsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;

/**
 * Provides all information about a specific application task which appears in the core task list.
 */
@Service
public class ApplicationTaskService {

  private final ApplicationContext applicationContext;

  @Autowired
  public ApplicationTaskService(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /**
   * Helper which asks Spring to provide the app specific task if its available.
   */
  private ApplicationFormSectionService getTaskService(ApplicationTask applicationTask) {
    if (applicationTask.getServiceClass() == null) {
      throw new IllegalStateException(String.format("Application task doesn't have service class specified: %s",
          applicationTask.name()));
    }

    return applicationContext.getBean(applicationTask.getServiceClass());
  }

  /**
   * A task can be shown for an application detail if the app type meets task criteria and the
   * app specific checks are met.
   */
  boolean canShowTask(ApplicationTask applicationTask, PwaApplicationDetail pwaApplicationDetail) {
    var taskService = getTaskService(applicationTask);
    var taskAppTypes = getValidApplicationTypesForTask(applicationTask);

    // Is the task valid for app type and does the specific app detail quality for task?
    return taskAppTypes.contains(pwaApplicationDetail.getPwaApplicationType())
        && taskService.canShowInTaskList(pwaApplicationDetail);
  }

  /**
   * Return a list of additional information about an applications task.
   */
  List<TaskInfo> getTaskInfoList(ApplicationTask applicationTask, PwaApplicationDetail pwaApplicationDetail) {
    return getTaskService(applicationTask).getTaskInfoList(pwaApplicationDetail);
  }

  /**
   * Return true when all questions answered under a task are valid for an application detail.
   */
  boolean isTaskComplete(ApplicationTask applicationTask, PwaApplicationDetail pwaApplicationDetail) {
    return getTaskService(applicationTask).isComplete(pwaApplicationDetail);
  }


  private Set<PwaApplicationType> getValidApplicationTypesForTask(ApplicationTask applicationTask) {
    return Optional.ofNullable(applicationTask.getControllerClass())
        // this allows us to test method logic by returning an arbitrary class from the applicationContext in tests
        .map(controllerClass -> applicationContext.getBean(controllerClass).getClass())
        .map(clazz -> clazz.getAnnotation(PwaApplicationTypeCheck.class))
        .map(typeCheck -> Set.of(typeCheck.types()))
        // task has valid app type if controller has no type restriction
        .orElse(EnumSet.allOf(PwaApplicationType.class));
  }

  String getRouteForTask(ApplicationTask task, PwaApplicationType applicationType, int applicationId) {
    Map<String, Object> uriVariables = new HashMap<>();
    uriVariables.put("applicationId", applicationId);
    switch (task) {
      case FIELD_INFORMATION:
        return ReverseRouter.route(on(PadPwaFieldsController.class)
            .renderFields(applicationType, applicationId, null, null, null));
      case APPLICATION_USERS:
        return ReverseRouter.route(on(PwaContactController.class)
            .renderContactsScreen(applicationType, applicationId, null));
      case PROJECT_INFORMATION:
        return ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(applicationType, applicationId, null, null));
      case FAST_TRACK:
        return ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(applicationType, applicationId, null, null, null));
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
        return ReverseRouter.route(on(PipelinesController.class)
            .renderPipelinesOverview(applicationId, applicationType, null));
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

}
