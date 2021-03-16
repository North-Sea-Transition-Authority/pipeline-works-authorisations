package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
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

  @Autowired
  public PublicNoticeOverviewController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
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
        () -> getPublicNoticeOverviewModelAndView(processingContext, authenticatedUserAccount));
  }


  private Map<String, String> getActionUrlMap(PwaAppProcessingContext processingContext) {

    var pwaApplicationId = processingContext.getMasterPwaApplicationId();
    var applicationType = processingContext.getApplicationType();

    return Map.of(
        PublicNoticeAction.NEW_DRAFT.name(), ReverseRouter.route(on(PublicNoticeDraftController.class)
            .renderDraftPublicNotice(pwaApplicationId, applicationType, null, null, null)),

        PublicNoticeAction.UPDATE_DRAFT.name(), ReverseRouter.route(on(PublicNoticeDraftController.class)
            .renderDraftPublicNotice(pwaApplicationId, applicationType, null, null, null)),

        PublicNoticeAction.APPROVE.name(), ReverseRouter.route(on(PublicNoticeApprovalController.class)
            .renderApprovePublicNotice(pwaApplicationId, applicationType, null, null, null)),

        PublicNoticeAction.REQUEST_DOCUMENT_UPDATE.name(), ReverseRouter.route(on(PublicNoticeDocumentUpdateRequestController.class)
            .renderRequestPublicNoticeDocumentUpdate(pwaApplicationId, applicationType, null, null, null)),

        PublicNoticeAction.FINALISE.name(), ReverseRouter.route(on(FinalisePublicNoticeController.class)
            .renderFinalisePublicNotice(pwaApplicationId, applicationType, null, null, null)),

        PublicNoticeAction.UPDATE_DATES.name(), ReverseRouter.route(on(FinalisePublicNoticeController.class)
            .renderUpdatePublicNoticePublicationDates(pwaApplicationId, applicationType, null, null, null)),

        PublicNoticeAction.WITHDRAW.name(), ReverseRouter.route(on(WithdrawPublicNoticeController.class)
            .renderWithdrawPublicNotice(pwaApplicationId, applicationType, null, null, null))
    );

  }



  private ModelAndView getPublicNoticeOverviewModelAndView(PwaAppProcessingContext processingContext,
                                                           AuthenticatedUserAccount authenticatedUserAccount) {

    var pwaApplication = processingContext.getPwaApplication();
    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(processingContext);
    var draftPublicNoticeUrl = ReverseRouter.route(on(PublicNoticeDraftController.class).renderDraftPublicNotice(
        pwaApplication.getId(), pwaApplication.getApplicationType(), null, authenticatedUserAccount, null));

    var modelAndView = new ModelAndView("publicNotice/publicNoticeOverview")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("allPublicNoticesView", allPublicNoticesView)
        .addObject("existingPublicNoticeActions", PublicNoticeAction.getExistingPublicNoticeActions())
        .addObject("actionUrlMap", getActionUrlMap(processingContext))
        .addObject("draftPublicNoticeUrl", draftPublicNoticeUrl)
        .addObject("backUrl", CaseManagementUtils.routeCaseManagement(processingContext))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Public notice overview");
    return modelAndView;
  }



}