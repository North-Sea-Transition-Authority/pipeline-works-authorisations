package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.form.publicnotice.WithdrawPublicNoticeForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.WithdrawPublicNoticeService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/withdraw-public-notice")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class WithdrawPublicNoticeController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final WithdrawPublicNoticeService withdrawPublicNoticeService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public WithdrawPublicNoticeController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      ControllerHelperService controllerHelperService,
      WithdrawPublicNoticeService withdrawPublicNoticeService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.withdrawPublicNoticeService = withdrawPublicNoticeService;
  }


  private ModelAndView publicNoticeInValidState(PwaAppProcessingContext processingContext,
                                                Supplier<ModelAndView> modelAndViewSupplier) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> {
          if (withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(processingContext.getPwaApplication())) {
            return modelAndViewSupplier.get();
          }
          throw new AccessDeniedException(
              String.format("Access denied as there is not an open public notice that can be withdrawn for application with id: %s",
                  processingContext.getMasterPwaApplicationId()));
        });
  }


  @GetMapping
  public ModelAndView renderWithdrawPublicNotice(@PathVariable("applicationId") Integer applicationId,
                                                @PathVariable("applicationType")
                                                @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                PwaAppProcessingContext processingContext,
                                                AuthenticatedUserAccount authenticatedUserAccount,
                                                @ModelAttribute("form") WithdrawPublicNoticeForm form) {

    return publicNoticeInValidState(processingContext, () ->
        getWithdrawPublicNoticeModelAndView(processingContext));
  }


  @PostMapping
  public ModelAndView postWithdrawPublicNotice(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaAppProcessingContext processingContext,
                                            AuthenticatedUserAccount authenticatedUserAccount,
                                            @ModelAttribute("form") WithdrawPublicNoticeForm form,
                                            BindingResult bindingResult) {

    return publicNoticeInValidState(processingContext, () -> {
      var validatedBindingResult = withdrawPublicNoticeService.validate(form, bindingResult);

      return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
          getWithdrawPublicNoticeModelAndView(processingContext), () -> {

            withdrawPublicNoticeService.withdrawPublicNotice(processingContext.getPwaApplication(), form, authenticatedUserAccount);
            return  ReverseRouter.redirect(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
                applicationId, pwaApplicationType, processingContext, authenticatedUserAccount));
          });
    });

  }


  private ModelAndView getWithdrawPublicNoticeModelAndView(PwaAppProcessingContext processingContext) {

    var pwaApplication = processingContext.getPwaApplication();
    var cancelUrl = ReverseRouter.route(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
        pwaApplication.getId(), pwaApplication.getApplicationType(), processingContext, null));

    var modelAndView = new ModelAndView("publicNotice/withdrawPublicNotice")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("cancelUrl", cancelUrl)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Withdraw public notice");
    return modelAndView;
  }




}