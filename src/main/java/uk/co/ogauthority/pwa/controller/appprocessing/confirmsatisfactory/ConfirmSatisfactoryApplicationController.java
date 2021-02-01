package uk.co.ogauthority.pwa.controller.appprocessing.confirmsatisfactory;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.form.appprocessing.confirmsatisfactory.ConfirmSatisfactoryApplicationForm;
import uk.co.ogauthority.pwa.service.appprocessing.application.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.appprocessing.confirmsatisfactory.ConfirmSatisfactoryApplicationFormValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/confirm-satisfactory")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.CONFIRM_SATISFACTORY_APPLICATION})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class ConfirmSatisfactoryApplicationController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;
  private final ControllerHelperService controllerHelperService;
  private final ConfirmSatisfactoryApplicationFormValidator confirmSatisfactoryApplicationFormValidator;

  @Autowired
  public ConfirmSatisfactoryApplicationController(ApplicationBreadcrumbService breadcrumbService,
                                                  ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService,
                                                  ControllerHelperService controllerHelperService,
                                                  ConfirmSatisfactoryApplicationFormValidator confirmSatisfactoryApplicationFormValidator) {
    this.breadcrumbService = breadcrumbService;
    this.confirmSatisfactoryApplicationService = confirmSatisfactoryApplicationService;
    this.controllerHelperService = controllerHelperService;
    this.confirmSatisfactoryApplicationFormValidator = confirmSatisfactoryApplicationFormValidator;
  }

  @GetMapping
  public ModelAndView renderConfirmSatisfactory(@PathVariable("applicationId") Integer applicationId,
                                                @PathVariable("applicationType")
                                                @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                PwaAppProcessingContext processingContext,
                                                AuthenticatedUserAccount authenticatedUserAccount,
                                                @ModelAttribute("form") ConfirmSatisfactoryApplicationForm form) {
    return whenConfirmable(
        processingContext,
        () -> getModelAndView(processingContext)
    );
  }

  @PostMapping
  public ModelAndView confirmSatisfactory(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          @ModelAttribute("form") ConfirmSatisfactoryApplicationForm form,
                                          BindingResult bindingResult,
                                          RedirectAttributes redirectAttributes) {

    confirmSatisfactoryApplicationFormValidator.validate(form, bindingResult);

    return whenConfirmable(processingContext,
        () -> controllerHelperService.checkErrorsAndRedirect(bindingResult, getModelAndView(processingContext),
            () -> {

              confirmSatisfactoryApplicationService.confirmSatisfactory(
                  processingContext.getApplicationDetail(),
                  form.getReason(),
                  authenticatedUserAccount.getLinkedPerson());

              FlashUtils.success(redirectAttributes, "Application confirmed satisfactory.");

              return CaseManagementUtils.redirectCaseManagement(processingContext);

            }));

  }

  private ModelAndView getModelAndView(PwaAppProcessingContext appProcessingContext) {

    var pwaApplicationDetail = appProcessingContext.getApplicationDetail();

    String cancelUrl = CaseManagementUtils.routeCaseManagement(appProcessingContext);

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/confirmSatisfactory/confirmSatisfactory")
        .addObject("errorList", List.of())
        .addObject("cancelUrl", cancelUrl)
        .addObject("caseSummaryView", appProcessingContext.getCaseSummaryView());

    breadcrumbService.fromCaseManagement(
        pwaApplicationDetail.getPwaApplication(),
        modelAndView,
        PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION.getTaskName());

    return modelAndView;

  }

  private ModelAndView whenConfirmable(PwaAppProcessingContext context, Supplier<ModelAndView> modelAndViewSupplier) {

    if (confirmSatisfactoryApplicationService.taskAccessible(context)) {
      return modelAndViewSupplier.get();
    } else {
      throw new AccessDeniedException(
          "Access denied as application not in confirm satisfactory state. app_id:" + context.getMasterPwaApplicationId()
      );
    }

  }

}