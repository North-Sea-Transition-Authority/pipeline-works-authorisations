package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.controller.PrototypePipelinesController;
import uk.co.ogauthority.pwa.temp.controller.PrototypePwaApplicationController;

@Service
public class ApplicationBreadcrumbService {

  public void fromWorkArea(ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, workArea(), thisPage);
  }

  public void fromTaskList(Integer applicationId, ModelAndView modelAndView, String thisPage) {
    addAttrs(modelAndView, taskList(applicationId), thisPage);
  }

  public void fromCrossingAgreements(Integer applicationId, ModelAndView modelAndView, String thisPage) {
    Map<String, String> breadcrumbs = taskList(applicationId);
    breadcrumbs.put(
        ReverseRouter.route(on(PrototypePwaApplicationController.class).viewCrossings(applicationId, null)),
        "Crossing agreements"
    );

    addAttrs(modelAndView, breadcrumbs, thisPage);
  }

  public void fromPwaContacts(Integer applicationId, ModelAndView modelAndView, String thisPage) {
    Map<String, String> breadcrumbs = taskList(applicationId);
    breadcrumbs.put(ReverseRouter.route(on(PrototypePwaApplicationController.class)
        .viewApplicationContacts(applicationId)), "PWA contacts");

    addAttrs(modelAndView, breadcrumbs, thisPage);
  }

  public void fromUoo(Integer applicationId, ModelAndView modelAndView, String thisPage) {
    Map<String, String> breadcrumbs = taskList(applicationId);
    breadcrumbs.put(
        ReverseRouter.route(on(PrototypePwaApplicationController.class).viewUserOwnerOperatorContacts(applicationId)),
        "Users, operator, owners"
    );

    addAttrs(modelAndView, breadcrumbs, thisPage);
  }

  public void fromPipelines(Integer applicationId, ModelAndView modelAndView, String thisPage) {
    Map<String, String> breadcrumbs = taskList(applicationId);
    breadcrumbs.put(ReverseRouter.route(on(PrototypePipelinesController.class).pipelines(applicationId)), "Pipelines");

    addAttrs(modelAndView, breadcrumbs, thisPage);
  }

  public void fromPipeline(Integer applicationId, String pipelineNumber,  ModelAndView modelAndView, String thisPage) {
    Map<String, String> breadcrumbs = taskList(applicationId);
    breadcrumbs.put(
        ReverseRouter.route(on(PrototypePipelinesController.class).pipelines(applicationId)),
        "Pipelines"
    );
    breadcrumbs.put(
        ReverseRouter.route(on(PrototypePipelinesController.class).editProductionPipelineRender(applicationId, pipelineNumber)),
        pipelineNumber
    );

    addAttrs(modelAndView, breadcrumbs, thisPage);
  }

  private Map<String, String> workArea() {
    Map<String, String> breadcrumbs = new LinkedHashMap<>();
    breadcrumbs.put(ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()), "Work area");
    return breadcrumbs;
  }

  private Map<String, String> taskList(Integer applicationId) {
    Map<String, String> breadcrumbs = workArea();
    breadcrumbs.put(ReverseRouter.route(on(PrototypePwaApplicationController.class).viewTaskList(applicationId)), "Task list");
    return breadcrumbs;
  }

  private void addAttrs(ModelAndView modelAndView, Map<String, String> breadcrumbs, String currentPage) {
    modelAndView.addObject("breadcrumbMap", breadcrumbs);
    modelAndView.addObject("currentPage", currentPage);
  }
}
