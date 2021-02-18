package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
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
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/public-notice-overview")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class PublicNoticeOverviewController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final PublicNoticeService publicNoticeService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PublicNoticeOverviewController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService,
      ControllerHelperService controllerHelperService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderPublicNoticeOverview(@PathVariable("applicationId") Integer applicationId,
                                                 @PathVariable("applicationType")
                                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 PwaAppProcessingContext processingContext,
                                                 AuthenticatedUserAccount authenticatedUserAccount) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> getDraftPublicNoticeModelAndView(processingContext, authenticatedUserAccount));
  }



  private ModelAndView getDraftPublicNoticeModelAndView(PwaAppProcessingContext processingContext,
                                                        AuthenticatedUserAccount authenticatedUserAccount) {

    var pwaApplication = processingContext.getPwaApplication();
    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(
        processingContext.getApplicationDetail(), authenticatedUserAccount);
    var draftPublicNoticeUrl = ReverseRouter.route(on(PublicNoticeDraftController.class).renderDraftPublicNotice(
        pwaApplication.getId(), pwaApplication.getApplicationType(), null, authenticatedUserAccount, null));

    var modelAndView = new ModelAndView("publicNotice/publicNoticeOverview")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("allPublicNoticesView", allPublicNoticesView)
        .addObject("existingPublicNoticeActions", PublicNoticeAction.getExistingPublicNoticeActions())
        .addObject("draftPublicNoticeUrl", draftPublicNoticeUrl)
        .addObject("backUrl", CaseManagementUtils.routeCaseManagement(processingContext))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Public notice overview");
    return modelAndView;
  }



}