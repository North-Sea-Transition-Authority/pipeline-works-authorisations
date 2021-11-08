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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.options.ChangeOptionsApprovalDeadlineForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ChangeOptionsApprovalDeadlineTaskService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.appprocessing.options.ChangeOptionsApprovalDeadlineFormValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/change-options-approval-deadline")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.CHANGE_OPTIONS_APPROVAL_DEADLINE})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class ChangeOptionsApprovalDeadlineController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final ApproveOptionsService approveOptionsService;
  private final ChangeOptionsApprovalDeadlineTaskService changeOptionsApprovalDeadlineTaskService;
  private final ChangeOptionsApprovalDeadlineFormValidator changeOptionsApprovalDeadlineFormValidator;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public ChangeOptionsApprovalDeadlineController(ApplicationBreadcrumbService breadcrumbService,
                                                 ApproveOptionsService approveOptionsService,
                                                 ChangeOptionsApprovalDeadlineTaskService changeOptionsApprovalDeadlineTaskService,
                                                 ChangeOptionsApprovalDeadlineFormValidator changeOptionsApprovalDeadlineFormValidator,
                                                 ControllerHelperService controllerHelperService) {
    this.breadcrumbService = breadcrumbService;
    this.approveOptionsService = approveOptionsService;
    this.changeOptionsApprovalDeadlineTaskService = changeOptionsApprovalDeadlineTaskService;
    this.changeOptionsApprovalDeadlineFormValidator = changeOptionsApprovalDeadlineFormValidator;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderChangeDeadline(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount,
                                           @ModelAttribute("form") ChangeOptionsApprovalDeadlineForm form) {

    return whenDeadlineChangeable(
        processingContext,
        () -> {
          var view = approveOptionsService.getOptionsApprovalDeadlineViewOrError(processingContext.getPwaApplication());

          DateUtils.setYearMonthDayFromInstant(
              form::setDeadlineDateYear,
              form::setDeadlineDateMonth,
              form::setDeadlineDateDay,
              view.getDeadlineInstant()
          );

          return getModelAndView(processingContext);
        }
    );
  }

  @PostMapping
  public ModelAndView changeOptionsApprovalDeadline(@PathVariable("applicationId") Integer applicationId,
                                                    @PathVariable("applicationType")
                                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                    PwaAppProcessingContext processingContext,
                                                    AuthenticatedUserAccount authenticatedUserAccount,
                                                    @ModelAttribute("form") ChangeOptionsApprovalDeadlineForm form,
                                                    BindingResult bindingResult) {

    changeOptionsApprovalDeadlineFormValidator.validate(form, bindingResult);

    return whenDeadlineChangeable(
        processingContext,
        () -> controllerHelperService.checkErrorsAndRedirect(
            bindingResult,
            getModelAndView(processingContext),
            () -> changeDeadlineAndRedirect(
                processingContext.getApplicationDetail(), authenticatedUserAccount, form
            )
        )
    );

  }

  private ModelAndView changeDeadlineAndRedirect(PwaApplicationDetail pwaApplicationDetail,
                                                 AuthenticatedUserAccount userAccount,
                                                 ChangeOptionsApprovalDeadlineForm form) {
    var deadlineDate = LocalDate.of(
        form.getDeadlineDateYear(),
        form.getDeadlineDateMonth(),
        form.getDeadlineDateDay()
    );

    var deadlineInstant = deadlineDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

    approveOptionsService.changeOptionsApprovalDeadline(pwaApplicationDetail, userAccount.getLinkedPerson(),
        deadlineInstant, form.getNote());

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

    var modelAndView = new ModelAndView("appprocessing/options/changeOptionsApprovalDeadline")
        .addObject("errorList", List.of())
        .addObject("cancelUrl", cancelUrl)
        .addObject("caseSummaryView", appProcessingContext.getCaseSummaryView());

    breadcrumbService.fromCaseManagement(
        pwaApplicationDetail.getPwaApplication(),
        modelAndView,
        PwaAppProcessingTask.APPROVE_OPTIONS.getTaskName());

    return modelAndView;
  }

  private ModelAndView whenDeadlineChangeable(PwaAppProcessingContext context,
                                              Supplier<ModelAndView> modelAndViewSupplier) {

    if (changeOptionsApprovalDeadlineTaskService.taskAccessible(context)) {
      return modelAndViewSupplier.get();
    } else {
      throw new AccessDeniedException(
          "Access denied as application cannot have deadline changed. app_id:" + context.getMasterPwaApplicationId()
      );
    }

  }


}