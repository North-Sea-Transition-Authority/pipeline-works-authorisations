package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.appprocessing.processingcharges.IndustryPaymentController;
import uk.co.ogauthority.pwa.controller.masterpwas.contacts.PwaContactController;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeApplicantViewController;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.model.view.banner.PageBannerView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDocumentUpdateService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.PwaAppProcessingTaskListService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TasksTabContentServiceTest {

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

  private TasksTabContentService taskTabContentService;

  private WebUserAccount wua;

  private PwaAppProcessingContext processingContext;

  private List<TaskListGroup> taskListGroupsList;

  @Before
  public void setUp() {

    taskTabContentService = new TasksTabContentService(
        taskListService,
        applicationUpdateRequestViewService,
        pwaApplicationRedirectService,
        approveOptionsService,
        publicNoticeDocumentUpdateService,
        publicNoticeService,
        consentReviewService);

    when(pwaApplicationRedirectService.getTaskListRoute(any())).thenReturn("#");

    wua = new WebUserAccount(1);

    processingContext = createContextWithPermissions(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);


  }

  @Test
  public void getTabContentModelMap_tasksTab_populated_industryOnlyPermission() {

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
  public void getTabContentModelMap_tasksTab_populated_industryAndRegulatorPermission() {

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
  public void getTabContentModelMap_tasksTab_populated_whenOptionsApproved() {

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
  public void getTabContentModelMap_tasksTab_populated_whenPublicNoticeUpdateRequested() {

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
  public void getTabContentModelMap_tasksTab_populated_whenPaymentPermission() {

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
  public void getTabContentModelMap_tasksTab_populated_whenManageAppContactsPermission() {

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
  public void getTabContentModelMap_tasksTab_populated_whenCaseManagementIndustryPermissionOnly() {

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
  public void getTabContentModelMap_tasksTab_populated_whenViewPublicNoticePermissionAndApplicantCanView() {

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
  public void getTabContentModelMap_tasksTab_populated_whenAppIsConsented_userTypeIsOga() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextFromInvolvementAndPermissions(
        ApplicationInvolvementDtoTestUtil.fromUserTypes(null, Set.of(UserType.OGA)),
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);
    when(consentReviewService.isApplicationConsented(any())).thenReturn(true);

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("consentHistoryUrl", ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
                processingContext.getPwaApplication().getMasterPwa().getId(), PwaViewTab.CONSENT_HISTORY,  null, null
            )))
        );
  }

  @Test
  public void getTabContentModelMap_tasksTab_populated_whenAppIsConsented_userInHolderTeam() {

    var taskListGroupsList = List.of(new TaskListGroup("test", 10, List.of()));

    var processingContext = createContextFromInvolvementAndPermissions(
        ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(null, Set.of(PwaOrganisationRole.APPLICATION_SUBMITTER)),
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);

    when(taskListService.getTaskListGroups(processingContext)).thenReturn(taskListGroupsList);
    when(consentReviewService.isApplicationConsented(any())).thenReturn(true);

    var modelMap = taskTabContentService.getTabContent(processingContext, AppProcessingTab.TASKS);

    verify(taskListService, times(1)).getTaskListGroups(processingContext);

    assertThat(modelMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("consentHistoryUrl", ReverseRouter.route(on(PwaViewController.class).renderViewPwa(
                processingContext.getPwaApplication().getMasterPwa().getId(), PwaViewTab.CONSENT_HISTORY,  null, null
            )))
        );
  }



  @Test
  public void getTabContentModelMap_differentTab_empty() {

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
        applicationInvolvementDto);
  }


}
