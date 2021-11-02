package uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.DECOMMISSIONING
})
public class CrossingAgreementsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final CrossingAgreementsService crossingAgreementsService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public CrossingAgreementsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      CrossingAgreementsService crossingAgreementsService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.crossingAgreementsService = crossingAgreementsService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getCrossingAgreementsModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/taskList")
        .addObject("tasks", crossingAgreementsService.getTaskListItems(detail))
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(detail.getPwaApplication()));
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView,
        "Blocks and crossing agreements");
    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderCrossingAgreementsOverview(@PathVariable("applicationType")
                                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                       @PathVariable("applicationId") Integer applicationId,
                                                       PwaApplicationContext applicationContext,
                                                       AuthenticatedUserAccount user) {
    return getCrossingAgreementsModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postOverview(@PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   @PathVariable("applicationId") Integer applicationId,
                                   PwaApplicationContext applicationContext,
                                   AuthenticatedUserAccount user) {
    var isComplete = crossingAgreementsService.isComplete(applicationContext.getApplicationDetail());
    if (!isComplete) {
      return getCrossingAgreementsModelAndView(applicationContext.getApplicationDetail())
          .addObject("errorMessage", "All sections must be completed");
    }
    return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
  }

}
