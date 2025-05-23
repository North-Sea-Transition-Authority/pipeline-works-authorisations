package uk.co.ogauthority.pwa.controller.appprocessing.options;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.CloseOutOptionsTaskService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/close-out-options")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.CLOSE_OUT_OPTIONS})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class CloseOutOptionsController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final CloseOutOptionsTaskService closeOutOptionsTaskService;
  private final ApproveOptionsService approveOptionsService;
  private final PadConfirmationOfOptionService padConfirmationOfOptionService;

  @Autowired
  public CloseOutOptionsController(ApplicationBreadcrumbService breadcrumbService,
                                   CloseOutOptionsTaskService closeOutOptionsTaskService,
                                   ApproveOptionsService approveOptionsService,
                                   PadConfirmationOfOptionService padConfirmationOfOptionService) {
    this.breadcrumbService = breadcrumbService;
    this.closeOutOptionsTaskService = closeOutOptionsTaskService;
    this.approveOptionsService = approveOptionsService;
    this.padConfirmationOfOptionService = padConfirmationOfOptionService;
  }

  @GetMapping
  public ModelAndView renderCloseOutOptions(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaAppProcessingContext processingContext,
                                            AuthenticatedUserAccount authenticatedUserAccount) {
    return whenCloseable(
        processingContext,
        () -> getModelAndView(processingContext)
    );
  }

  @PostMapping
  public ModelAndView closeOutOptions(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      PwaAppProcessingContext processingContext,
                                      AuthenticatedUserAccount authenticatedUserAccount,
                                      RedirectAttributes redirectAttributes) {
    return whenCloseable(
        processingContext,
        () -> {
          approveOptionsService.closeOutOptions(processingContext.getApplicationDetail(), authenticatedUserAccount);
          FlashUtils.success(
              redirectAttributes,
              String.format("Closed application %s", processingContext.getApplicationDetail().getPwaApplicationRef())
          );
          return ReverseRouter.redirect(on(WorkAreaController.class)
              .renderWorkArea(null, null, null));
        }
    );
  }

  private ModelAndView getModelAndView(PwaAppProcessingContext appProcessingContext) {

    var pwaApplicationDetail = appProcessingContext.getApplicationDetail();

    String cancelUrl = CaseManagementUtils.routeCaseManagement(appProcessingContext);

    var modelAndView = new ModelAndView("appprocessing/options/closeOutOptions")
        .addObject("cancelUrl", cancelUrl)
        .addObject("caseSummaryView", appProcessingContext.getCaseSummaryView())
        .addObject("padConfirmationOfOptionView",
            padConfirmationOfOptionService.getPadConfirmationOfOptionView(pwaApplicationDetail));

    breadcrumbService.fromCaseManagement(
        pwaApplicationDetail.getPwaApplication(),
        modelAndView,
        PwaAppProcessingTask.CLOSE_OUT_OPTIONS.getTaskName());

    return modelAndView;
  }

  private ModelAndView whenCloseable(PwaAppProcessingContext context, Supplier<ModelAndView> modelAndViewSupplier) {

    if (closeOutOptionsTaskService.taskAccessible(context)) {
      return modelAndViewSupplier.get();
    } else {
      throw new AccessDeniedException(
          "Access denied as application not in options close-able state. app_id:" + context.getMasterPwaApplicationId()
      );
    }

  }

}