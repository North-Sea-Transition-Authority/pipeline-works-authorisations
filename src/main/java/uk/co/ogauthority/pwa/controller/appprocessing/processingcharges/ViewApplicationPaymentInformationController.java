package uk.co.ogauthority.pwa.controller.appprocessing.processingcharges;

import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/view-app-payment")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.VIEW_PAYMENT_DETAILS_IF_EXISTS)
public class ViewApplicationPaymentInformationController {

  private static final String PAGE_REF = "View payment information";

  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final ApplicationPaymentSummariser applicationPaymentSummariser;
  private final ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public ViewApplicationPaymentInformationController(ApplicationChargeRequestService applicationChargeRequestService,
                                                     ApplicationPaymentSummariser applicationPaymentSummariser,
                                                     ApplicationBreadcrumbService breadcrumbService) {
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.applicationPaymentSummariser = applicationPaymentSummariser;
    this.breadcrumbService = breadcrumbService;
  }


  @GetMapping
  public ModelAndView renderPaymentInformation(@PathVariable("applicationId") Integer applicationId,
                                               @PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               PwaAppProcessingContext processingContext) {

    return whenAppPaidFor(processingContext,
        applicationChargeRequestReport -> {
          var displayableAppCharges = applicationPaymentSummariser.summarise(applicationChargeRequestReport);

          var modelAndView = new ModelAndView("appprocessing/processingcharges/viewPaymentDetails")
              .addObject("caseSummaryView", processingContext.getCaseSummaryView())
              .addObject("appRef", processingContext.getCaseSummaryView().getPwaApplicationRef())
              .addObject("appPaymentDisplaySummary", displayableAppCharges)
              .addObject("pageRef", PAGE_REF);

          breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, PAGE_REF);
          return modelAndView;
        });
  }

  private ModelAndView whenAppPaidFor(PwaAppProcessingContext pwaAppProcessingContext,
                                      Function<ApplicationChargeRequestReport, ModelAndView> modelAndViewFunction) {

    var appChargeRequestReport = applicationChargeRequestService
        .getLatestRequestAsApplicationChargeRequestReport(pwaAppProcessingContext.getPwaApplication())
        .filter(r -> r.getPwaAppChargeRequestStatus() == PwaAppChargeRequestStatus.PAID)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Could not find PAID for charge request for appId:" + pwaAppProcessingContext.getMasterPwaApplicationId()));

    return modelAndViewFunction.apply(appChargeRequestReport);
  }

}
