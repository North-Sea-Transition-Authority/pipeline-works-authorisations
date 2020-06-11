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

  @Autowired
  public PipelinesHuooController(
      ApplicationBreadcrumbService breadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.breadcrumbService = breadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  @GetMapping
  public ModelAndView renderSummary(@PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("applicationId") int applicationId,
                                    PwaApplicationContext applicationContext) {
    return createSummaryModelAndView(applicationContext, false);
  }

  private ModelAndView createSummaryModelAndView(PwaApplicationContext applicationContext, boolean doValidation) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinehuoo/pipelineHuooSummary")
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(applicationContext.getPwaApplication()))
        .addObject("pageHeading", ApplicationTask.PIPELINES_HUOO.getDisplayName())
        .addObject("markCompleteErrorMessage", doValidation ? "Please correct errors" : null);
    breadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView, "Campaign Works");

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
