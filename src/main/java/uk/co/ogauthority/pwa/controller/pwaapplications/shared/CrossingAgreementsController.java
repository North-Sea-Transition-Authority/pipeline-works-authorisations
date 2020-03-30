package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.MedianLineCrossingController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
public class CrossingAgreementsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;

  @Autowired
  public CrossingAgreementsController(
      ApplicationBreadcrumbService applicationBreadcrumbService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
  }

  private ModelAndView getCrossingAgreementsModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/overview")
        .addObject("medianLineUrl", ReverseRouter.route(on(MedianLineCrossingController.class)
            .renderMedianLineForm(detail.getPwaApplicationType(), null, null, null)));
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Crossings");
    return modelAndView;
  }

  @GetMapping
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderCrossingAgreementsOverview(@PathVariable("applicationType")
                                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                       PwaApplicationContext applicationContext,
                                                       AuthenticatedUserAccount user) {
    return getCrossingAgreementsModelAndView(applicationContext.getApplicationDetail());
  }

}
