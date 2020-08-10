package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.HuooController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.LocationDetailsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.campaignworks.CampaignWorksController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CrossingAgreementsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PipelinesHuooController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelineIdentsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.TechnicalDrawingsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.tasklist.CrossingAgreementsTaskListService;

@Service
public class ApplicationBreadcrumbService {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @Autowired
  public ApplicationBreadcrumbService(
      PwaApplicationRedirectService pwaApplicationRedirectService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
  }

  public void fromWorkArea(ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, workArea(), thisPage);
  }

  public void fromCrossings(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(pwaApplication.getApplicationType(), pwaApplication.getId(), null, null)),
        ApplicationTask.CROSSING_AGREEMENTS.getDisplayName());
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromCrossingSection(PwaApplicationDetail detail, ModelAndView modelAndView, CrossingAgreementTask task,
                                  String thisPage) {
    var map = taskList(detail.getPwaApplication());
    map.put(ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null,
                null)),
        ApplicationTask.CROSSING_AGREEMENTS.getDisplayName());
    map.put(crossingAgreementsTaskListService.getRoute(detail, task), task.getDisplayText());
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromLocationDetails(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(LocationDetailsController.class)
            .renderLocationDetails(pwaApplication.getApplicationType(), null, null, null)),
        ApplicationTask.LOCATION_DETAILS.getDisplayName());
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromTechnicalDrawings(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(pwaApplication.getApplicationType(), pwaApplication.getId(), null, null)),
        ApplicationTask.TECHNICAL_DRAWINGS.getDisplayName());
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromHuoo(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(HuooController.class)
            .renderHuooSummary(pwaApplication.getApplicationType(), pwaApplication.getId(), null, null)),
        ApplicationTask.HUOO.getDisplayName());
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromPipelinesOverview(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(PipelinesController.class)
        .renderPipelinesOverview(pwaApplication.getId(), pwaApplication.getApplicationType(), null)), "Pipelines");
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromPipelinesHuooOverview(PwaApplication pwaApplication, ModelAndView modelAndView, String thisPage) {
    var map = taskList(pwaApplication);
    map.put(
        ReverseRouter.route(on(PipelinesHuooController.class).renderSummary(
            pwaApplication.getApplicationType(),
            pwaApplication.getId(),
            null)
        ),
        ApplicationTask.PIPELINES_HUOO.getDisplayName()
    );
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromPipelineIdentOverview(
      PwaApplication pwaApplication,
      PadPipeline padPipeline,
      ModelAndView modelAndView,
      String thisPage) {
    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(PipelinesController.class)
        .renderPipelinesOverview(pwaApplication.getId(), pwaApplication.getApplicationType(), null)), "Pipelines");
    map.put(ReverseRouter.route(on(PipelineIdentsController.class)
            .renderIdentOverview(pwaApplication.getId(), pwaApplication.getApplicationType(), padPipeline.getId(), null)),
        padPipeline.getPipelineRef() + " idents");
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromCampaignWorksOverview(
      PwaApplication pwaApplication,
      ModelAndView modelAndView,
      String thisPage) {
    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(CampaignWorksController.class)
        .renderSummary(pwaApplication.getApplicationType(), pwaApplication.getId(), null)), "Campaign works");
    addAttrs(modelAndView, map, thisPage);
  }

  public void fromDepositsOverview(
      PwaApplication pwaApplication,
      ModelAndView modelAndView,
      String thisPage) {

    var map = taskList(pwaApplication);
    map.put(ReverseRouter.route(on(PermanentDepositController.class)
        .renderPermanentDepositsOverview(pwaApplication.getApplicationType(), pwaApplication.getId(), null, null)),
        ApplicationTask.PERMANENT_DEPOSITS.getDisplayName());
    addAttrs(modelAndView, map, thisPage);

  }

  public void fromTaskList(PwaApplication application, ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, taskList(application), thisPage);
  }

  private Map<String, String> taskList(PwaApplication application) {
    var map = workArea();
    String route = pwaApplicationRedirectService.getTaskListRoute(application);
    map.put(route, "Task list");
    return map;
  }

  private Map<String, String> workArea() {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null)), "Work area");
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }

}
