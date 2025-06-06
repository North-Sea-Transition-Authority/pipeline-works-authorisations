package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.PadPipelineTaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.PipelineUrlFactory;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
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
  private final PadPipelineTaskListService padPipelineTaskListService;
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PwaApplicationRedirectService applicationRedirectService;

  @Autowired
  public PipelinesTaskListController(PadPipelineTaskListService padPipelineTaskListService,
                                     ApplicationBreadcrumbService breadcrumbService,
                                     PwaApplicationRedirectService applicationRedirectService) {
    this.padPipelineTaskListService = padPipelineTaskListService;
    this.breadcrumbService = breadcrumbService;
    this.applicationRedirectService = applicationRedirectService;
  }

  private ModelAndView getOverviewModelAndView(PwaApplicationContext applicationContext) {

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/overview")
        .addObject("pipelineTaskListItems", padPipelineTaskListService.getSortedPipelineTaskListItems(applicationContext))
        .addObject("pipelineUrlFactory", new PipelineUrlFactory(applicationContext.getApplicationDetail()))
        .addObject("canImportConsentedPipeline", padPipelineTaskListService
            .canImportConsentedPipelines(applicationContext.getApplicationDetail()))
        .addObject("taskListUrl", applicationRedirectService.getTaskListRoute(applicationContext.getPwaApplication()));

    breadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView, "Pipelines");

    return modelAndView;

  }

  @GetMapping
  public ModelAndView renderPipelinesOverview(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaApplicationContext applicationContext,
                                              RedirectAttributes redirectAttributes) {
    return getOverviewModelAndView(applicationContext);
  }

  @PostMapping
  public ModelAndView postPipelinesOverview(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();

    var pipelineSummaryValidationResult = padPipelineTaskListService.getValidationResult(detail);

    if (!pipelineSummaryValidationResult.isSectionComplete()) {
      return getOverviewModelAndView(applicationContext)
          .addObject("pipelineSummaryValidationResult", pipelineSummaryValidationResult);
    }

    return applicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());

  }

}
