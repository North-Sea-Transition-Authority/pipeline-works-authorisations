package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller;

import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/view-app-payment")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.VIEW_PAYMENT_DETAILS_IF_EXISTS)
public class ViewApplicationPaymentInformationController {

  private static final String PAGE_REF = "Payment information";

  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final ApplicationPaymentSummariser applicationPaymentSummariser;
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PersonService personService;

  @Autowired
  public ViewApplicationPaymentInformationController(ApplicationChargeRequestService applicationChargeRequestService,
                                                     ApplicationPaymentSummariser applicationPaymentSummariser,
                                                     ApplicationBreadcrumbService breadcrumbService,
                                                     PersonService personService) {
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.applicationPaymentSummariser = applicationPaymentSummariser;
    this.breadcrumbService = breadcrumbService;
    this.personService = personService;
  }


  @GetMapping
  public ModelAndView renderPaymentInformation(@PathVariable("applicationId") Integer applicationId,
                                               @PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               PwaAppProcessingContext processingContext) {

    return whenAppPaidFor(processingContext,
        applicationChargeRequestReport -> {
          var displayableAppCharges = applicationPaymentSummariser.summarise(applicationChargeRequestReport);

          var paidByPersonView = personService.getSimplePersonView(applicationChargeRequestReport.getPaidByPersonId());

          var modelAndView = new ModelAndView("appprocessing/processingcharges/viewPaymentDetails")
              .addObject("caseSummaryView", processingContext.getCaseSummaryView())
              .addObject("appRef", processingContext.getCaseSummaryView().getPwaApplicationRef())
              .addObject("appPaymentDisplaySummary", displayableAppCharges)
              .addObject("pageRef", PAGE_REF)
              .addObject("caseManagementUrl", CaseManagementUtils.routeCaseManagement(processingContext))
              .addObject("requestedInstant", DateUtils.formatDateTime(applicationChargeRequestReport.getRequestedInstant()))
              .addObject("paidByName", paidByPersonView.getName())
              .addObject("paidByEmail", paidByPersonView.getEmail())
              .addObject("paidInstant", DateUtils.formatDateTime(applicationChargeRequestReport.getPaidInstant()))
              .addObject("paymentStatus", applicationChargeRequestReport.getPwaAppChargeRequestStatus().getDispayString());

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
