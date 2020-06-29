package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PadPipelineHuooViewFactory;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipeline-huoo")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.HUOO_VARIATION
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class PipelinesHuooController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPipelinesHuooService padPipelinesHuooService;
  private final PadPipelineHuooViewFactory padPipelineHuooViewFactory;

  @Autowired
  public PipelinesHuooController(
      ApplicationBreadcrumbService breadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PadPipelinesHuooService padPipelinesHuooService,
      PadPipelineHuooViewFactory padPipelineHuooViewFactory) {
    this.breadcrumbService = breadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPipelinesHuooService = padPipelinesHuooService;
    this.padPipelineHuooViewFactory = padPipelineHuooViewFactory;
  }

  @GetMapping
  public ModelAndView renderSummary(@PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("applicationId") int applicationId,
                                    PwaApplicationContext applicationContext) {
    return createSummaryModelAndView(applicationContext, false);
  }

  private ModelAndView createSummaryModelAndView(PwaApplicationContext applicationContext, boolean doValidation) {

    var pipelineAndOrgGroupAppSummary = padPipelinesHuooService.createPipelineAndOrganisationRoleGroupSummary(
        applicationContext.getApplicationDetail());

    var pipelineHuooSummaryView = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(
        applicationContext.getApplicationDetail(),
        pipelineAndOrgGroupAppSummary
    );

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinehuoo/pipelineHuooSummary")
        .addObject("holderSummary", pipelineHuooSummaryView.getHolderRoleSummaryView())
        .addObject("userSummary", pipelineHuooSummaryView.getUserRoleSumaryView())
        .addObject("operatorSummary", pipelineHuooSummaryView.getOperatorRoleSummaryView())
        .addObject("ownerSummary", pipelineHuooSummaryView.getOwnerRoleSummaryView())
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(applicationContext.getPwaApplication()))
        .addObject("pageHeading", ApplicationTask.PIPELINES_HUOO.getDisplayName() + " (HUOO)")
        .addObject("markCompleteErrorMessage", doValidation ? "Please correct errors" : null)
        .addObject("urlFactory", new PipelineHuooUrlFactory(
            applicationContext.getMasterPwaApplicationId(),
            applicationContext.getApplicationType())
        );
    breadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView,
        ApplicationTask.PIPELINES_HUOO.getDisplayName());

    return modelAndView;
  }

  @PostMapping
  public ModelAndView postSummary(@PathVariable("applicationType")
                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                  @PathVariable("applicationId") int applicationId,
                                  PwaApplicationContext applicationContext) {
    return createSummaryModelAndView(applicationContext, true);
  }

}
