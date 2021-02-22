package uk.co.ogauthority.pwa.controller.appprocessing.processingcharges;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/app-payment")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.PAY_FOR_APPLICATION)
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT)
public class IndustryPaymentController {

  private static final String PAY_FOR_APP_LANDING_PAGE = "Pay for application";
  private final ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public IndustryPaymentController(ApplicationBreadcrumbService breadcrumbService) {
    this.breadcrumbService = breadcrumbService;
  }


  @GetMapping
  public ModelAndView renderPayForApplicationLanding(@PathVariable("applicationId") Integer applicationId,
                                                     @PathVariable("applicationType")
                                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                     PwaAppProcessingContext processingContext) {
    var modelAndView = new ModelAndView("appprocessing/processingcharges/payForApplicationLanding")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("appRef", processingContext.getPwaApplication().getAppReference())
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(processingContext))
        .addObject("errorList", List.of());

    breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, PAY_FOR_APP_LANDING_PAGE);
    return modelAndView;

  }

  @PostMapping
  public ModelAndView startPaymentAttempt(@PathVariable("applicationId") Integer applicationId,
                                                     @PathVariable("applicationType")
                                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                     PwaAppProcessingContext processingContext) {
    return ReverseRouter.redirect(on(IndustryPaymentController.class)
        .renderPayForApplicationLanding(applicationId, pwaApplicationType, null));

  }

}
