package uk.co.ogauthority.pwa.controller.appprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.application.WithdrawApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.WithdrawPublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/withdraw-application")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.WITHDRAW_APPLICATION})
public class WithdrawApplicationController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final WithdrawApplicationService withdrawApplicationService;
  private final ControllerHelperService controllerHelperService;
  private final WithdrawPublicNoticeService withdrawPublicNoticeService;

  @Autowired
  public WithdrawApplicationController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                       WithdrawApplicationService withdrawApplicationService,
                                       ControllerHelperService controllerHelperService,
                                       WithdrawPublicNoticeService withdrawPublicNoticeService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.withdrawApplicationService = withdrawApplicationService;
    this.controllerHelperService = controllerHelperService;
    this.withdrawPublicNoticeService = withdrawPublicNoticeService;
  }


  @GetMapping
  public ModelAndView renderWithdrawApplication(@PathVariable("applicationId") Integer applicationId,
                                                @PathVariable("applicationType")
                                                @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                PwaAppProcessingContext processingContext,
                                                AuthenticatedUserAccount authenticatedUserAccount,
                                                @ModelAttribute("form") WithdrawApplicationForm form) {

    return getWithdrawApplicationModelAndView(processingContext);
  }


  @PostMapping
  public ModelAndView postWithdrawApplication(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaAppProcessingContext processingContext,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") WithdrawApplicationForm form,
                                              BindingResult bindingResult,
                                              RedirectAttributes redirectAttributes) {

    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(processingContext.getPwaApplication());
    if (publicNoticeCanBeWithdrawn) {
      var errorItem = new ErrorItem(0, "", "You must withdraw any open public notices before withdrawing this application");
      return getWithdrawApplicationModelAndView(processingContext).addObject("errorList", List.of(errorItem));
    }

    bindingResult = withdrawApplicationService.validate(form, bindingResult, processingContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getWithdrawApplicationModelAndView(processingContext), () -> {
      withdrawApplicationService.withdrawApplication(form, processingContext.getPwaApplication(), authenticatedUserAccount);

      FlashUtils.info(redirectAttributes, processingContext.getApplicationDetail().getPwaApplicationRef() + " withdrawn");
      return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));

    });

  }

  private ModelAndView getWithdrawApplicationModelAndView(PwaAppProcessingContext processingContext) {

    var pwaApplicationDetail = processingContext.getApplicationDetail();

    String cancelUrl = ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            AppProcessingTab.TASKS,
            null,
            null));

    var modelAndView = new ModelAndView("appprocessing/withdrawApplication");
    modelAndView.addObject("errorList", List.of())
        .addObject("appRef", pwaApplicationDetail.getPwaApplicationRef())
        .addObject("cancelUrl", cancelUrl)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    applicationBreadcrumbService.fromCaseManagement(
        pwaApplicationDetail.getPwaApplication(),
        modelAndView,
        PwaAppProcessingTask.WITHDRAW_APPLICATION.getTaskName());

    return modelAndView;
  }





}