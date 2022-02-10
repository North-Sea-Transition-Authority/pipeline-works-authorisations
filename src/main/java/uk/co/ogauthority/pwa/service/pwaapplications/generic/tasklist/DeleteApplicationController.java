package uk.co.ogauthority.pwa.service.pwaapplications.generic.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationDeleteService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/delete-application")
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class DeleteApplicationController {

  private final PwaApplicationDeleteService pwaApplicationDeleteService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService applicationRedirectService;

  @Autowired
  public DeleteApplicationController(
      PwaApplicationDeleteService pwaApplicationDeleteService,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PwaApplicationRedirectService applicationRedirectService) {
    this.pwaApplicationDeleteService = pwaApplicationDeleteService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.applicationRedirectService = applicationRedirectService;
  }

  @GetMapping
  public ModelAndView renderDeleteApplication(@PathVariable("applicationType")
                                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                  @PathVariable("applicationId") Integer applicationId,
                                                  PwaApplicationContext applicationContext) {

    var modelAndView = new ModelAndView("pwaApplication/shared/deleteApplication")
        .addObject("appRef", applicationContext.getApplicationDetail().getPwaApplicationRef())
        .addObject("taskListUrl", applicationRedirectService.getTaskListRoute(applicationContext.getPwaApplication()));

    applicationBreadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView,
        "Delete application");

    return modelAndView;
  }


  @PostMapping
  public ModelAndView postDeleteApplication(@PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            RedirectAttributes redirectAttributes) {


    pwaApplicationDeleteService.deleteApplication(applicationContext.getUser(), applicationContext.getApplicationDetail());
    FlashUtils.info(redirectAttributes,"Deleted application " + applicationContext.getApplicationDetail().getPwaApplicationRef());
    return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));
  }





}
