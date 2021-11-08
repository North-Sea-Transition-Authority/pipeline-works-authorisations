package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.controller;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.PipelineAndOrgRoleGroupViewsByRole;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.PipelineHuooScreenValidationResultFactory;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.PipelineHuooUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.PipelineHuooValidationResult;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipeline-huoo")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.HUOO_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class PipelinesHuooController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PipelinesHuooController.class);

  private final ApplicationBreadcrumbService breadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPipelinesHuooService padPipelinesHuooService;
  private final PipelineHuooScreenValidationResultFactory pipelineHuooScreenValidationResultFactory;

  @Autowired
  public PipelinesHuooController(
      ApplicationBreadcrumbService breadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PadPipelinesHuooService padPipelinesHuooService,
      PipelineHuooScreenValidationResultFactory pipelineHuooScreenValidationResultFactory) {
    this.breadcrumbService = breadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPipelinesHuooService = padPipelinesHuooService;
    this.pipelineHuooScreenValidationResultFactory = pipelineHuooScreenValidationResultFactory;
  }

  @GetMapping
  public ModelAndView renderSummary(@PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("applicationId") int applicationId,
                                    PwaApplicationContext applicationContext) {
    var pipelineHuooSummaryView = padPipelinesHuooService.getPadPipelinesHuooSummaryView(applicationContext.getApplicationDetail());
    return createSummaryModelAndView(applicationContext, pipelineHuooSummaryView, null);
  }

  private ModelAndView createSummaryModelAndView(PwaApplicationContext applicationContext,
                                                 PipelineAndOrgRoleGroupViewsByRole pipelineHuooSummaryView,
                                                 @Nullable PipelineHuooValidationResult pipelineHuooValidationResult) {

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinehuoo/pipelineHuooSummary")
        .addObject("holderSummary", pipelineHuooSummaryView.getHolderRoleSummaryView())
        .addObject("userSummary", pipelineHuooSummaryView.getUserRoleSummaryView())
        .addObject("operatorSummary", pipelineHuooSummaryView.getOperatorRoleSummaryView())
        .addObject("ownerSummary", pipelineHuooSummaryView.getOwnerRoleSummaryView())
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(applicationContext.getPwaApplication()))
        .addObject("pageHeading", ApplicationTask.PIPELINES_HUOO.getDisplayName() + " (HUOO)")
        .addObject("urlFactory", new PipelineHuooUrlFactory(
            applicationContext.getMasterPwaApplicationId(),
            applicationContext.getApplicationType())
        );

    if (pipelineHuooValidationResult != null) {
      var screenResult =  pipelineHuooScreenValidationResultFactory.createFromValidationResult(pipelineHuooValidationResult);
      modelAndView.addObject("summaryValidationResult", screenResult);
    }

    breadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView,
        ApplicationTask.PIPELINES_HUOO.getDisplayName());

    return modelAndView;
  }

  @PostMapping
  public ModelAndView postSummary(@PathVariable("applicationType")
                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                  @PathVariable("applicationId") int applicationId,
                                  PwaApplicationContext applicationContext) {

    LOGGER.debug("Starting POST");

    var pipelineHuooSummaryView = padPipelinesHuooService.getPadPipelinesHuooSummaryView(
        applicationContext.getApplicationDetail());

    LOGGER.debug("Retrieved pipeline huoo summary view: {}", pipelineHuooSummaryView.toString());

    var validationResult = padPipelinesHuooService.generatePipelineHuooValidationResult(
        applicationContext.getApplicationDetail(), pipelineHuooSummaryView
    );

    LOGGER.debug("Retrieved validation result: {}", validationResult.toString());

    if (validationResult.isValid()) {
      return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
    } else {
      return createSummaryModelAndView(applicationContext, pipelineHuooSummaryView, validationResult);
    }
  }

}
