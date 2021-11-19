package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeApprovalService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeApprovalResult;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/public-notice-approval")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class PublicNoticeApprovalController  {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeApprovalService publicNoticeApprovalService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PublicNoticeApprovalController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService,
      PublicNoticeApprovalService publicNoticeApprovalService,
      ControllerHelperService controllerHelperService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
    this.publicNoticeApprovalService = publicNoticeApprovalService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderApprovePublicNotice(@PathVariable("applicationId") Integer applicationId,
                                                @PathVariable("applicationType")
                                                @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                PwaAppProcessingContext processingContext,
                                                AuthenticatedUserAccount authenticatedUserAccount,
                                                @ModelAttribute("form") PublicNoticeApprovalForm form) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> {
          if (publicNoticeApprovalService.openPublicNoticeCanBeApproved(processingContext.getPwaApplication())) {
            return getApprovePublicNoticeModelAndView(processingContext);
          }
          throw new AccessDeniedException(
              "Access denied as there is not an open public notice in the approval stage for application with id: " +
                  processingContext.getMasterPwaApplicationId());
        });
  }


  @PostMapping
  public ModelAndView postApprovePublicNotice(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaAppProcessingContext processingContext,
                                            AuthenticatedUserAccount authenticatedUserAccount,
                                            @ModelAttribute("form") PublicNoticeApprovalForm form,
                                            BindingResult bindingResult) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,  () -> {
          if (publicNoticeApprovalService.openPublicNoticeCanBeApproved(processingContext.getPwaApplication())) {
            var validatedBindingResult = publicNoticeApprovalService.validate(form, bindingResult);

            return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
                getApprovePublicNoticeModelAndView(processingContext), () -> {
                  publicNoticeApprovalService.updatePublicNoticeRequest(
                      form, processingContext.getPwaApplication(), authenticatedUserAccount);
                  return  ReverseRouter.redirect(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
                      applicationId, pwaApplicationType, processingContext, authenticatedUserAccount));
                });

          }
          throw new AccessDeniedException(
              "Access denied as there is not an open public notice in the approval stage for application with id: " +
                  processingContext.getMasterPwaApplicationId());

        });

  }


  private ModelAndView getApprovePublicNoticeModelAndView(PwaAppProcessingContext processingContext) {

    var publicNotice = publicNoticeService.getLatestPublicNotice(processingContext.getPwaApplication());
    var publicNoticeRequest = publicNoticeService.getLatestPublicNoticeRequest(publicNotice);

    var pwaApplication = processingContext.getPwaApplication();
    var publicNoticeOverviewUrl = ReverseRouter.route(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
        pwaApplication.getId(), pwaApplication.getApplicationType(), null, null));

    var modelAndView = new ModelAndView("publicNotice/approvePublicNotice")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("coverLetter", publicNoticeRequest.getCoverLetterText())
        .addObject("requestReason", publicNoticeRequest.getReason().getReasonText())
        .addObject("approvalResultOptions", PwaApplicationPublicNoticeApprovalResult.asList())
        .addObject("cancelUrl", publicNoticeOverviewUrl)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Review public notice request");
    return modelAndView;
  }






}