package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.controller.pwaapplications.rest.PipelineRestController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineHeaderConditionalQuestion;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineControllerRouteUtils;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineHeaderFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineHeaderValidationHints;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineRemovalService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist.PadPipelineTaskListService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class PipelinesTaskListController {

  private final PadPipelineService padPipelineService;
  private final PipelineRemovalService pipelineRemovalService;

  private final PadPipelineTaskListService padPipelineTaskListService;

  private final ApplicationBreadcrumbService breadcrumbService;
  private final PipelineHeaderFormValidator pipelineHeaderFormValidator;
  private final PwaApplicationRedirectService applicationRedirectService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PipelinesTaskListController(PadPipelineService padPipelineService,
                                     PipelineRemovalService pipelineRemovalService,
                                     PadPipelineTaskListService padPipelineTaskListService,
                                     ApplicationBreadcrumbService breadcrumbService,
                                     PipelineHeaderFormValidator pipelineHeaderFormValidator,
                                     PwaApplicationRedirectService applicationRedirectService,
                                     ControllerHelperService controllerHelperService) {
    this.padPipelineService = padPipelineService;
    this.pipelineRemovalService = pipelineRemovalService;
    this.padPipelineTaskListService = padPipelineTaskListService;
    this.breadcrumbService = breadcrumbService;
    this.pipelineHeaderFormValidator = pipelineHeaderFormValidator;
    this.applicationRedirectService = applicationRedirectService;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView getOverviewModelAndView(PwaApplicationDetail detail) {

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/overview")
        .addObject("pipelineTaskListItems", padPipelineTaskListService.getPipelineTaskListItems(detail).stream()
            .sorted(Comparator.comparing(PipelineOverview::getPipelineNumber))
            .collect(Collectors.toList()))
        .addObject("pipelineUrlFactory", new PipelineUrlFactory(detail))
        .addObject("canImportConsentedPipeline", padPipelineTaskListService.canImportConsentedPipelines(detail))
        .addObject("taskListUrl", applicationRedirectService.getTaskListRoute(detail.getPwaApplication()));

    breadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Pipelines");

    return modelAndView;

  }

  @GetMapping
  public ModelAndView renderPipelinesOverview(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaApplicationContext applicationContext) {
    return getOverviewModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postPipelinesOverview(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();

    var pipelineSummaryValidationResult = padPipelineTaskListService.getValidationResult(detail);

    if (!pipelineSummaryValidationResult.isSectionComplete()) {
      return getOverviewModelAndView(applicationContext.getApplicationDetail())
          .addObject("pipelineSummaryValidationResult", pipelineSummaryValidationResult);
    }

    return applicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());

  }

}
