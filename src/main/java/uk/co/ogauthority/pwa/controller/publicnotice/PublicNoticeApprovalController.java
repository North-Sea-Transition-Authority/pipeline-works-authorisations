package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/public-notice-approval")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class PublicNoticeApprovalController  {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final PublicNoticeService publicNoticeService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PublicNoticeApprovalController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService,
      ControllerHelperService controllerHelperService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
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
        () -> getApprovePublicNoticeModelAndView(processingContext));
  }


  private ModelAndView getApprovePublicNoticeModelAndView(PwaAppProcessingContext processingContext) {

    var pwaApplication = processingContext.getPwaApplication();
    var publicNoticeOverviewUrl = ReverseRouter.route(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
        pwaApplication.getId(), pwaApplication.getApplicationType(), null, null));

    var modelAndView = new ModelAndView("publicNotice/approvePublicNotice")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("cancelUrl", publicNoticeOverviewUrl)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Public notice approval");
    return modelAndView;
  }






}