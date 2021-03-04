package uk.co.ogauthority.pwa.controller.appprocessing.processingcharges;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.CancelPaymentRequestAppProcessingService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeException;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.CancelAppChargeForm;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/cancel-app-payment")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.CANCEL_PAYMENT)
public class CancelPaymentRequestController {

  private static final String PAGE_REF = PwaAppProcessingTask.CANCEL_PAYMENT.getTaskName();

  private final CancelPaymentRequestAppProcessingService cancelPaymentRequestProcService;
  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final ApplicationPaymentSummariser applicationPaymentSummariser;
  private final ControllerHelperService controllerHelperService;
  private final ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public CancelPaymentRequestController(CancelPaymentRequestAppProcessingService cancelPaymentRequestProcService,
                                        ApplicationChargeRequestService applicationChargeRequestService,
                                        ApplicationPaymentSummariser applicationPaymentSummariser,
                                        ControllerHelperService controllerHelperService,
                                        ApplicationBreadcrumbService breadcrumbService) {
    this.cancelPaymentRequestProcService = cancelPaymentRequestProcService;
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.applicationPaymentSummariser = applicationPaymentSummariser;
    this.controllerHelperService = controllerHelperService;
    this.breadcrumbService = breadcrumbService;
  }

  @GetMapping
  public ModelAndView renderCancelPaymentRequest(@PathVariable("applicationId") Integer applicationId,
                                                 @PathVariable("applicationType")
                                                 @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 PwaAppProcessingContext processingContext,
                                                 @ModelAttribute("form") CancelAppChargeForm form) {

    return whenAccessible(processingContext, applicationChargeRequestReport -> {

      var displayableAppCharges = applicationPaymentSummariser.summarise(applicationChargeRequestReport);

      var modelAndView =  new ModelAndView("appprocessing/processingcharges/cancelPaymentRequest")
          .addObject("caseSummaryView", processingContext.getCaseSummaryView())
          .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(processingContext))
          .addObject("appRef", processingContext.getCaseSummaryView().getPwaApplicationRef())
          .addObject("appPaymentDisplaySummary", displayableAppCharges)
          .addObject("pageRef", PAGE_REF)
          .addObject("errorList", List.of());

      breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, PAGE_REF);

      return modelAndView;
    });
  }

  @PostMapping
  public ModelAndView cancelPaymentRequest(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           @ModelAttribute("form") CancelAppChargeForm form,
                                           BindingResult bindingResult) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        ReverseRouter.redirect(on(this.getClass()).renderCancelPaymentRequest(
            processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null, null)),
        () -> whenAccessible(processingContext, applicationChargeRequestReport -> {
          // TODO PWA-978 placeholder until actual cancel implemented
          return ReverseRouter.redirect(on(this.getClass()).renderCancelPaymentRequest(
              processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null, null));
        })
    );

  }


  private ModelAndView whenAccessible(PwaAppProcessingContext processingContext,
                                      Function<ApplicationChargeRequestReport, ModelAndView> reportModelAndViewFunction) {
    if (!cancelPaymentRequestProcService.taskAccessible(processingContext)) {
      throw new AccessDeniedException(
          String.format("WuaId:%s authorised to access Cancel payments task for appId:%s",
              processingContext.getUser().getWuaId(),
              processingContext.getMasterPwaApplicationId()
          )
      );
    }

    var appChargeReport = applicationChargeRequestService.getApplicationChargeRequestReport(
        processingContext.getPwaApplication())
        .orElseThrow(() -> new ApplicationChargeException(
            "Expected to be able to generate app charge report for appId:" + processingContext.getMasterPwaApplicationId())
        );

    return reportModelAndViewFunction.apply(appChargeReport);

  }
}
