package uk.co.ogauthority.pwa.controller.appprocessing.initialreview;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@RequestMapping("/pwa-application-processing/{applicationType}/{applicationId}/initial-review")
@Controller
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW})
public class InitialReviewController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final InitialReviewService initialReviewService;

  @Autowired
  public InitialReviewController(ApplicationBreadcrumbService breadcrumbService,
                                 InitialReviewService initialReviewService) {
    this.breadcrumbService = breadcrumbService;
    this.initialReviewService = initialReviewService;
  }

  private ModelAndView getInitialReviewModelAndView(PwaApplicationDetail detail) {

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/initialReview/initialReview")
        .addObject("appRef", detail.getPwaApplicationRef())
        .addObject("isOptionsVariation", detail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION))
        .addObject("isFastTrack", false) //TODO PWA-542
        .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)));

    breadcrumbService.fromWorkArea(modelAndView, detail.getPwaApplicationRef());

    return modelAndView;

  }

  @GetMapping
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
  public ModelAndView renderInitialReview(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          AuthenticatedUserAccount user) {
    return getInitialReviewModelAndView(processingContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postInitialReview(@PathVariable("applicationId") Integer applicationId,
                                        @PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        PwaAppProcessingContext processingContext,
                                        AuthenticatedUserAccount user) {

    try {
      initialReviewService.acceptApplication(processingContext.getApplicationDetail(), user);
    } catch (ActionAlreadyPerformedException e) {
      // TODO PWA-565 flash messages
    }

    return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));

  }

}
