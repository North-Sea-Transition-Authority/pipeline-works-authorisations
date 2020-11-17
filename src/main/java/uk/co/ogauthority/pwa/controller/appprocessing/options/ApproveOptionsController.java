package uk.co.ogauthority.pwa.controller.appprocessing.options;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.options.ApproveOptionsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.appprocessing.options.ApproveOptionsFormValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/approve-options")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.APPROVE_OPTIONS})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class ApproveOptionsController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final ApproveOptionsService approveOptionsService;

  private final ApproveOptionsFormValidator approveOptionsFormValidator;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public ApproveOptionsController(ApplicationBreadcrumbService breadcrumbService,
                                  ApproveOptionsService approveOptionsService,
                                  ApproveOptionsFormValidator approveOptionsFormValidator,
                                  ControllerHelperService controllerHelperService) {

    this.breadcrumbService = breadcrumbService;
    this.approveOptionsService = approveOptionsService;
    this.approveOptionsFormValidator = approveOptionsFormValidator;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderApproveOptions(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount,
                                           @ModelAttribute("form") ApproveOptionsForm form) {
    return whenApprovable(
        processingContext,
        () -> getModelAndView(processingContext)
    );
  }

  @PostMapping
  public ModelAndView approveOptions(@PathVariable("applicationId") Integer applicationId,
                                     @PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     PwaAppProcessingContext processingContext,
                                     AuthenticatedUserAccount authenticatedUserAccount,
                                     @ModelAttribute("form") ApproveOptionsForm form,
                                     BindingResult bindingResult) {

    approveOptionsFormValidator.validate(form, bindingResult);

    return whenApprovable(
        processingContext,
        () -> controllerHelperService.checkErrorsAndRedirect(
            bindingResult,
            getModelAndView(processingContext),
            () -> approveInitialOptionsAndRedirect(
                processingContext.getApplicationDetail(), authenticatedUserAccount, form
            )
        )
    );

  }

  private ModelAndView approveInitialOptionsAndRedirect(PwaApplicationDetail pwaApplicationDetail,
                                                        AuthenticatedUserAccount userAccount,
                                                        ApproveOptionsForm approveOptionsForm) {

    var deadlineDate = LocalDate.of(
        approveOptionsForm.getDeadlineDateYear(),
        approveOptionsForm.getDeadlineDateMonth(),
        approveOptionsForm.getDeadlineDateDay()
    );

    var deadlineInstant = deadlineDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

    approveOptionsService.approveOptions(pwaApplicationDetail, userAccount.getLinkedPerson(), deadlineInstant);

    return ReverseRouter.redirect(on(CaseManagementController.class)
        .renderCaseManagement(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            AppProcessingTab.TASKS,
            null,
            null
        )
    );
  }

  private ModelAndView getModelAndView(PwaAppProcessingContext appProcessingContext) {

    var pwaApplicationDetail = appProcessingContext.getApplicationDetail();

    String cancelUrl = ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            AppProcessingTab.TASKS,
            null,
            null));

    var modelAndView = new ModelAndView("appprocessing/options/approveOptions")
        .addObject("errorList", List.of())
        .addObject("cancelUrl", cancelUrl)
        .addObject("caseSummaryView", appProcessingContext.getCaseSummaryView());

    breadcrumbService.fromCaseManagement(
        pwaApplicationDetail.getPwaApplication(),
        modelAndView,
        PwaAppProcessingTask.APPROVE_OPTIONS.getTaskName());

    return modelAndView;

  }

  private ModelAndView whenApprovable(PwaAppProcessingContext context, Supplier<ModelAndView> modelAndViewSupplier) {

    if (approveOptionsService.taskAccessible(context)) {
      return modelAndViewSupplier.get();
    } else {
      throw new AccessDeniedException(
          "Access denied as application not in options approve-able state. app_id:" + context.getMasterPwaApplicationId()
      );
    }

  }


}