package uk.co.ogauthority.pwa.features.appprocessing.casemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.controller.asbuilt.ReopenAsBuiltNotificationGroupController;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeApplicantViewController;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.appcontacts.controller.PwaContactController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransfer;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller.IndustryPaymentController;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTaskListService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.view.banner.PageBannerView;
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
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TasksTabContentServiceTest {

  @Mock
  private PwaAppProcessingTaskListService taskListService;

  @Mock
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Mock
  private ApproveOptionsService approveOptionsService;

  @Mock
  private PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private ConsentReviewService consentReviewService;

  @Mock
  private ApplicationChargeRequestService applicationChargeRequestService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private AsBuiltViewerService asBuiltViewerService;

  @Mock
  private AsBuiltNotificationAuthService asBuiltNotificationAuthService;

  @Mock
  private PadPipelineTransferService pipelineTransferService;

  private TasksTabContentService taskTabContentService;

  private WebUserAccount wua;

  private PwaAppProcessingContext processingContext;

  private List<TaskListGroup> taskListGroupsList;

  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil
      .createGroupWithConsent_fromNgId(10);

  @BeforeEach
  void setUp() {

    taskTabContentService = new TasksTabContentService(
        taskListService,
        applicationUpdateRequestViewService,
        pwaApplicationRedirectService,
        approveOptionsService,
        publicNoticeDocumentUpdateService,
        publicNoticeService,
        consentReviewService,
        applicationChargeRequestService,
        pwaConsentService,
        asBuiltViewerService,
        asBuiltNotificationAuthService, pipelineTransferService);

    when(pwaApplicationRedirectService.getTaskListRoute(any())).thenReturn("#");

    wua = new WebUserAccount(1);

    processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);


  }

  @Test
  void getTabContentModelMap_tasksTab_populated_industryOnlyPermission() {

    var involvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(null, Set.of(
        ApplicationInvolvementDtoTestUtil.InvolvementFlag.INDUSTRY_INVOLVEMENT_ONLY));
    processingContext = createContextFromInvolvementAndPermissions(
        involvement,
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);

    var requestView = mock(ApplicationUpdateRequestView.class);

    when(applicationUpdateRequestViewService.getOpenRequestView(any(PwaApplicationDetail.class))).thenReturn(Optional.of(requestView));

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsOnly(
            tuple("taskListGroups", taskListGroupsList),
            tuple("industryFlag", true),
            tuple("updateRequestView", requestView),
            tuple("taskListUrl", "#")
        );

  }

  @Test
  void getTabContentModelMap_tasksTab_populated_industryAndRegulatorPermission() {

    processingContext = createContextFromInvolvementAndPermissions(
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(null),
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY,
        PwaAppProcessingPermission.CASE_MANAGEMENT_OGA
    );

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("industryFlag", false)
        );

  }

  @Test
  void getTabContentModelMap_tasksTab_populated_whenOptionsApproved() {

    var optionsApprovedBanner = new PageBannerView.PageBannerViewBuilder().build();
    when(approveOptionsService.getOptionsApprovalPageBannerView(any(PwaApplicationDetail.class)))
        .thenReturn(Optional.of(optionsApprovedBanner));

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("optionsApprovalPageBanner", optionsApprovedBanner)
        );

  }

  @Test
  void getTabContentModelMap_tasksTab_populated_whenPublicNoticeUpdateRequested() {

    processingContext = createContextWithPermissions(
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY, PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);

    var publicNoticePageBannerView = new PageBannerView.PageBannerViewBuilder().build();
    when(publicNoticeDocumentUpdateService.getPublicNoticeUpdatePageBannerView(processingContext.getPwaApplication()))
        .thenReturn(Optional.of(publicNoticePageBannerView));

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("publicNoticePageBannerView", publicNoticePageBannerView)
        );

  }


  @Test
  void getTabContentModelMap_tasksTab_populated_whenPaymentPermission() {

    processingContext = createContextWithPermissions(
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY,
        PwaAppProcessingPermission.PAY_FOR_APPLICATION
    );

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);

    var optionsApprovedBanner = new PageBannerView.PageBannerViewBuilder().build();
    when(approveOptionsService.getOptionsApprovalPageBannerView(any(PwaApplicationDetail.class)))
        .thenReturn(Optional.of(optionsApprovedBanner));
    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("payForAppUrl", ReverseRouter.route(on(IndustryPaymentController.class).renderPayForApplicationLanding(
                processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null
            )))
        );

  }

  @Test
  void getTabContentModelMap_tasksTab_populated_whenManageAppContactsPermission() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextWithPermissions(
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY,
        PwaAppProcessingPermission.MANAGE_APPLICATION_CONTACTS
    );

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);

    var optionsApprovedBanner = new PageBannerView.PageBannerViewBuilder().build();
    when(approveOptionsService.getOptionsApprovalPageBannerView(any(PwaApplicationDetail.class)))
        .thenReturn(Optional.of(optionsApprovedBanner));
    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("manageAppContactsUrl", ReverseRouter.route(on(PwaContactController.class).renderContactsScreen(
                processingContext.getApplicationType(), processingContext.getMasterPwaApplicationId(),null, null
            )))
        );
  }

  @Test
  void getTabContentModelMap_tasksTab_populated_whenCaseManagementIndustryPermissionOnly() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextWithPermissions(
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY
    );

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);

    var optionsApprovedBanner = new PageBannerView.PageBannerViewBuilder().build();
    when(approveOptionsService.getOptionsApprovalPageBannerView(any(PwaApplicationDetail.class)))
        .thenReturn(Optional.of(optionsApprovedBanner));
    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap).doesNotContainKeys("manageAppContactsUrl", "payForAppUrl");

  }


  @Test
  void getTabContentModelMap_tasksTab_populated_whenViewPublicNoticePermissionAndApplicantCanView() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextWithPermissions(
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY,
        PwaAppProcessingPermission.VIEW_PUBLIC_NOTICE
    );

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);
    when(publicNoticeService.canApplicantViewLatestPublicNotice(processingContext.getPwaApplication())).thenReturn(true);

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("viewPublicNoticeUrl", ReverseRouter.route(on(PublicNoticeApplicantViewController.class).renderViewPublicNotice(
                processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(),  null, null
            )))
        );
  }


  @Test
  void getTabContentModelMap_tasksTab_populated_whenAppIsConsented_userTypeIsOga() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = new PwaAppProcessingContext(
        PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL),
        wua,
        Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY),
        null,
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(null),
        Set.of(UserType.OGA));

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);
    when(consentReviewService.isApplicationConsented(any())).thenReturn(true);
    when(pwaConsentService.getConsentByPwaApplication(processingContext.getPwaApplication())).thenReturn(Optional.empty());

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("consentHistoryUrl", ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
                processingContext.getPwaApplication().getMasterPwa().getId(), PwaViewTab.CONSENT_HISTORY,  null, null, null
            )))
        );
    assertThat(modelMap).doesNotContainKeys("reopenAsBuiltGroupUrl");
  }

  @Test
  void getTabContentModelMap_tasksTab_populated_whenAppIsConsented_userInHolderTeam() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextFromInvolvementAndPermissions(
        ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(null, Set.of(PwaOrganisationRole.APPLICATION_SUBMITTER)),
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);
    when(consentReviewService.isApplicationConsented(any())).thenReturn(true);
    when(pwaConsentService.getConsentByPwaApplication(processingContext.getPwaApplication())).thenReturn(Optional.empty());

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("consentHistoryUrl", ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
                processingContext.getPwaApplication().getMasterPwa().getId(), PwaViewTab.CONSENT_HISTORY,  null, null, null
            )))
        );

    assertThat(modelMap).doesNotContainKeys("reopenAsBuiltGroupUrl");
  }

  @Test
  void getTabContentModelMap_tasksTab_populated_whenAppIsConsented_userIsNotAsBuiltAdmin_notificationGroupCannotBeReopened() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextFromInvolvementAndPermissions(
        ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(null, Set.of(PwaOrganisationRole.APPLICATION_SUBMITTER)),
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);
    when(consentReviewService.isApplicationConsented(any())).thenReturn(true);
    when(pwaConsentService.getConsentByPwaApplication(processingContext.getPwaApplication()))
        .thenReturn(Optional.of(asBuiltNotificationGroup.getPwaConsent()));
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(wua.getLinkedPerson())).thenReturn(false);

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("consentHistoryUrl", ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
                processingContext.getPwaApplication().getMasterPwa().getId(), PwaViewTab.CONSENT_HISTORY,  null, null, null
            ))));

    assertThat(modelMap).doesNotContainKeys("reopenAsBuiltGroupUrl");
  }

  @Test
  void getTabContentModelMap_tasksTab_populated_whenAppIsConsented_userIsAsBuiltAdmin_notificationGroupCanBeReopened() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextFromInvolvementAndPermissions(
        ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(null, Set.of(PwaOrganisationRole.APPLICATION_SUBMITTER)),
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);
    when(consentReviewService.isApplicationConsented(any())).thenReturn(true);
    when(pwaConsentService.getConsentByPwaApplication(processingContext.getPwaApplication()))
        .thenReturn(Optional.of(asBuiltNotificationGroup.getPwaConsent()));
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(wua.getLinkedPerson())).thenReturn(true);
    when(asBuiltViewerService.canGroupBeReopened(asBuiltNotificationGroup.getPwaConsent())).thenReturn(true);
    when(asBuiltViewerService.getNotificationGroupOptionalFromConsent(asBuiltNotificationGroup.getPwaConsent()))
        .thenReturn(Optional.of(asBuiltNotificationGroup));

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("consentHistoryUrl", ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
                processingContext.getPwaApplication().getMasterPwa().getId(), PwaViewTab.CONSENT_HISTORY,  null, null, null
            ))),
            tuple("reopenAsBuiltGroupUrl", ReverseRouter.route(on(ReopenAsBuiltNotificationGroupController.class)
                .renderReopenAsBuiltNotificationForm(asBuiltNotificationGroup.getId(), null)))
        );
  }

  @Test
  void getTabContentModelMap_tasksTab_populated_whenUserHasViewPaymentPriv_andAppIsPaidFor() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextFromInvolvementAndPermissions(
        ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(null, Set.of(PwaOrganisationRole.APPLICATION_SUBMITTER)),
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY, PwaAppProcessingPermission.VIEW_PAYMENT_DETAILS_IF_EXISTS);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);
    when(applicationChargeRequestService.applicationChargeRequestCompleteAndPaid(any())).thenReturn(true);

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    assertThat(modelMap).containsKey("viewAppPaymentUrl");
  }

  @Test
  void getTabContentModelMap_tasksTab_populated_whenUserDoesNotHaveViewPaymentPriv() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextFromInvolvementAndPermissions(
        ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(null, Set.of(PwaOrganisationRole.APPLICATION_SUBMITTER)),
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    assertThat(modelMap).doesNotContainKey("viewAppPaymentUrl");
  }

  @Test
  void getTabContentModelMap_differentTab_empty() {

    var involvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(null, Set.of(
        ApplicationInvolvementDtoTestUtil.InvolvementFlag.INDUSTRY_INVOLVEMENT_ONLY));
    var processingContext = createContextFromInvolvementAndPermissions(
        involvement,
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.CASE_HISTORY);

    verifyNoInteractions(taskListService);

    assertThat(modelMap)
        .doesNotContainKey("payForAppUrl")
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("taskListGroups", List.of()),
            tuple("industryFlag", true),
            tuple("taskListUrl", "")
        );

  }

  @Test
  void getTabContentModelMap_pipelineTransfer_empty() {

    when(pipelineTransferService.findUnclaimedByDonorApplication(processingContext.getApplicationDetail()))
        .thenReturn(Collections.emptyList());
    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    assertThat(modelMap).doesNotContainKey("pipelineTransferPageBannerView");
  }

  @Test
  void getTabContentModelMap_pipelineTransfer_uncompletedTransfers() {
    //Setup Pipeline Transfer
    var pipelineNumber = "PL111";
    var piplineTransfers = List.of(new PadPipelineTransfer());
    when(pipelineTransferService.findUnclaimedByDonorApplication(processingContext.getApplicationDetail()))
        .thenReturn(piplineTransfers);
    when(pipelineTransferService.getUnclaimedPipelineNumbers(piplineTransfers)).thenReturn(List.of(pipelineNumber));
    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    assertThat(modelMap).containsKey("pipelineTransferPageBannerView");
    var bannerView = (NotificationBannerView) modelMap.get("pipelineTransferPageBannerView");

    assertThat(bannerView.getBodyLines()).hasSize(2);
    assertThat(bannerView.getBodyLines().get(1).getLineText()).isEqualTo(pipelineNumber);
  }

  private PwaAppProcessingContext createContextWithPermissions(PwaAppProcessingPermission... permissions) {
    return createContextFromInvolvementAndPermissions(
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(null),
        permissions
    );
  }

  private PwaAppProcessingContext createContextFromInvolvementAndPermissions(ApplicationInvolvementDto applicationInvolvementDto,
                                                                             PwaAppProcessingPermission... permissions) {
    return new PwaAppProcessingContext(
        PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL),
        wua,
        Set.of(permissions),
        null,
        applicationInvolvementDto,
        Set.of());
  }



}
