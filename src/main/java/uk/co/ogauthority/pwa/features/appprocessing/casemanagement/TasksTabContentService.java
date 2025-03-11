package uk.co.ogauthority.pwa.features.appprocessing.casemanagement;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.asbuilt.ReopenAsBuiltNotificationGroupController;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeApplicantViewController;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.features.application.tasks.appcontacts.controller.PwaContactController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller.IndustryPaymentController;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller.ViewApplicationPaymentInformationController;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTaskListService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.view.banner.PageBannerView;
import uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerBodyLine;
import uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDocumentUpdateService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationAuthService;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;

@Service
public class TasksTabContentService implements AppProcessingTabContentService {

  private final PwaAppProcessingTaskListService appProcessingTaskListService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ApproveOptionsService approveOptionsService;
  private final PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService;
  private final PublicNoticeService publicNoticeService;
  private final ConsentReviewService consentReviewService;
  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final PwaConsentService pwaConsentService;
  private final AsBuiltViewerService asBuiltViewerService;
  private final AsBuiltNotificationAuthService asBuiltNotificationAuthService;
  private final PadPipelineTransferService pipelineTransferService;

  @Autowired
  public TasksTabContentService(PwaAppProcessingTaskListService appProcessingTaskListService,
                                ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
                                PwaApplicationRedirectService pwaApplicationRedirectService,
                                ApproveOptionsService approveOptionsService,
                                PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService,
                                PublicNoticeService publicNoticeService,
                                ConsentReviewService consentReviewService,
                                ApplicationChargeRequestService applicationChargeRequestService,
                                PwaConsentService pwaConsentService,
                                AsBuiltViewerService asBuiltViewerService,
                                AsBuiltNotificationAuthService asBuiltNotificationAuthService,
                                PadPipelineTransferService pipelineTransferService) {
    this.appProcessingTaskListService = appProcessingTaskListService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.approveOptionsService = approveOptionsService;
    this.publicNoticeDocumentUpdateService = publicNoticeDocumentUpdateService;
    this.publicNoticeService = publicNoticeService;
    this.consentReviewService = consentReviewService;
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.pwaConsentService = pwaConsentService;
    this.asBuiltViewerService = asBuiltViewerService;
    this.asBuiltNotificationAuthService = asBuiltNotificationAuthService;
    this.pipelineTransferService = pipelineTransferService;
  }

  @Override
  public Map<String, Object> getTabContent(PwaAppProcessingContext appProcessingContext, AppProcessingTab currentTab) {

    List<TaskListGroup> taskListGroups = List.of();
    Optional<ApplicationUpdateRequestView> updateRequestViewOpt = Optional.empty();
    Optional<PageBannerView> optionsApprovalPageBannerViewOpt = Optional.empty();
    Optional<PageBannerView> publicNoticePageBannerViewOpt = Optional.empty();
    Optional<NotificationBannerView> pipelineTransferViewOpt = Optional.empty();
    String taskListUrl = "";

    Optional<String> payForAppUrl = Optional.empty();
    Optional<String> manageAppContactsUrl = Optional.empty();
    Optional<String> viewPublicNoticeUrl = Optional.empty();
    Optional<String> consentHistoryUrl = Optional.empty();
    Optional<String> viewAppPaymentUrl = Optional.empty();
    Optional<String> reopenAsBuiltGroupUrl = Optional.empty();

    boolean industryFlag = appProcessingContext.getApplicationInvolvement().hasOnlyIndustryInvolvement();

    // only retrieve tasks if we're on the tasks tab to reduce load time
    if (currentTab == AppProcessingTab.TASKS) {

      taskListGroups = appProcessingTaskListService.getTaskListGroups(appProcessingContext);

      updateRequestViewOpt = applicationUpdateRequestViewService.getOpenRequestView(appProcessingContext.getApplicationDetail());

      optionsApprovalPageBannerViewOpt = approveOptionsService.getOptionsApprovalPageBannerView(
          appProcessingContext.getApplicationDetail()
      );

      if (appProcessingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC)) {
        publicNoticePageBannerViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeUpdatePageBannerView(
            appProcessingContext.getPwaApplication()
        );
      }

      taskListUrl = pwaApplicationRedirectService.getTaskListRoute(appProcessingContext.getPwaApplication());

      if (appProcessingContext.hasProcessingPermission(PwaAppProcessingPermission.PAY_FOR_APPLICATION)) {
        payForAppUrl = Optional.of(ReverseRouter.route(on(IndustryPaymentController.class).renderPayForApplicationLanding(
            appProcessingContext.getMasterPwaApplicationId(), appProcessingContext.getApplicationType(), null
        )));
      }

      if (appProcessingContext.hasProcessingPermission(PwaAppProcessingPermission.MANAGE_APPLICATION_CONTACTS)) {
        manageAppContactsUrl = Optional.of(ReverseRouter.route(on(PwaContactController.class).renderContactsScreen(
            appProcessingContext.getApplicationType(), appProcessingContext.getMasterPwaApplicationId(), null, null
        )));
      }

      if (appProcessingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.VIEW_PUBLIC_NOTICE)
          && publicNoticeService.canApplicantViewLatestPublicNotice(appProcessingContext.getPwaApplication())) {
        viewPublicNoticeUrl =
            Optional.of(ReverseRouter.route(on(PublicNoticeApplicantViewController.class).renderViewPublicNotice(
                appProcessingContext.getMasterPwaApplicationId(), appProcessingContext.getApplicationType(), null, null
            )));
      }

      var unclaimedPipelineTransfers = pipelineTransferService.findUnclaimedByDonorApplication(appProcessingContext.getApplicationDetail());
      if (!unclaimedPipelineTransfers.isEmpty()) {
        var bodyLine = new NotificationBannerBodyLine(
            "Cannot progress application until the following pipeline transfers have been claimed by a different application:", null);

        var pipelineTransferBannerBuilder = new NotificationBannerView.BannerBuilder("Awaiting pipeline transfer claim")
            .addBodyLine(bodyLine);

        for (var unclaimedTransferReference : pipelineTransferService.getUnclaimedPipelineNumbers(unclaimedPipelineTransfers)) {
          var transferLine = new NotificationBannerBodyLine(
              unclaimedTransferReference,
              "govuk-!-font-weight-bold govuk-list--bullet"
          );
          pipelineTransferBannerBuilder.addBodyLine(transferLine);
        }
        pipelineTransferViewOpt = Optional.of(pipelineTransferBannerBuilder.build());
      }

      if (consentReviewService.isApplicationConsented(appProcessingContext.getApplicationDetail())
          && (appProcessingContext.getUserTypes().contains(UserType.OGA)
          || appProcessingContext.getApplicationInvolvement().isUserInHolderTeam())) {
        consentHistoryUrl = Optional.of(ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
            appProcessingContext.getPwaApplication().getMasterPwa().getId(), PwaViewTab.CONSENT_HISTORY, null, null, null
        )));

        reopenAsBuiltGroupUrl = getReopenAsBuiltNotificationGroupUrl(appProcessingContext);
      }

      if (appProcessingContext.hasProcessingPermission(PwaAppProcessingPermission.VIEW_PAYMENT_DETAILS_IF_EXISTS)
          && applicationChargeRequestService.applicationChargeRequestCompleteAndPaid(appProcessingContext.getPwaApplication())) {
        viewAppPaymentUrl = Optional.of(
            ReverseRouter.route(on(ViewApplicationPaymentInformationController.class).renderPaymentInformation(
                appProcessingContext.getMasterPwaApplicationId(), appProcessingContext.getApplicationType(), null
            ))
        );
      }
    }

    var modelMap = new HashMap<>(Map.of(
        "taskListGroups", taskListGroups,
        "industryFlag", industryFlag,
        "taskListUrl", taskListUrl
    ));

    updateRequestViewOpt.ifPresent(view -> modelMap.put("updateRequestView", view));
    optionsApprovalPageBannerViewOpt.ifPresent(view -> modelMap.put("optionsApprovalPageBanner", view));
    publicNoticePageBannerViewOpt.ifPresent(view -> modelMap.put("publicNoticePageBannerView", view));
    pipelineTransferViewOpt.ifPresent(view -> modelMap.put("pipelineTransferPageBannerView", view));
    payForAppUrl.ifPresent(s -> modelMap.put("payForAppUrl", s));
    manageAppContactsUrl.ifPresent(s -> modelMap.put("manageAppContactsUrl", s));
    viewPublicNoticeUrl.ifPresent(s -> modelMap.put("viewPublicNoticeUrl", s));
    consentHistoryUrl.ifPresent(s -> modelMap.put("consentHistoryUrl", s));
    viewAppPaymentUrl.ifPresent(s -> modelMap.put("viewAppPaymentUrl", s));
    reopenAsBuiltGroupUrl.ifPresent(s -> modelMap.put("reopenAsBuiltGroupUrl", s));

    return modelMap;

  }

  private Optional<String> getReopenAsBuiltNotificationGroupUrl(PwaAppProcessingContext appProcessingContext) {
    var consentOptional = pwaConsentService.getConsentByPwaApplication(appProcessingContext.getPwaApplication());
    if (consentOptional.isPresent()) {
      var consent = consentOptional.get();
      var isOgaAdmin = asBuiltNotificationAuthService.isUserAsBuiltNotificationAdmin(appProcessingContext.getUser());
      if (isOgaAdmin && asBuiltViewerService.canGroupBeReopened(consent)) {
        return asBuiltViewerService.getNotificationGroupOptionalFromConsent(consent)
            .map(asBuiltGroup -> ReverseRouter.route(on(ReopenAsBuiltNotificationGroupController.class)
                .renderReopenAsBuiltNotificationForm(asBuiltGroup.getId(), null)));
      }
    }
    return Optional.empty();
  }

}
