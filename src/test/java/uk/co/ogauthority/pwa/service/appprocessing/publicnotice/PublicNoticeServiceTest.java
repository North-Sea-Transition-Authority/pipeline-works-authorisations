package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.PublicNoticeApprovalRequestEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRepository;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.template.TemplateTextService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDraftValidator;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeServiceTest {

  private PublicNoticeService publicNoticeService;

  @Mock
  private TemplateTextService templateTextService;

  @Mock
  private PublicNoticeDraftValidator validator;

  @Mock
  private AppFileService appFileService;

  @Mock
  private PublicNoticeRepository publicNoticeRepository;

  @Mock
  private PublicNoticeRequestRepository publicNoticeRequestRepository;

  @Mock
  private PublicNoticeDocumentRepository publicNoticeDocumentRepository;

  @Mock
  private PublicNoticeDocumentLinkRepository publicNoticeDocumentLinkRepository;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private Clock clock;

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private TeamService teamService;

  @Mock
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private static AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocument> publicNoticeDocumentArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocumentLink> publicNoticeDocumentLinkArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeRequest> publicNoticeRequestArgumentCaptor;


  @Before
  public void setUp() {
    clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    publicNoticeService = new PublicNoticeService(
        templateTextService,
        validator,
        appFileService,
        publicNoticeRepository,
        publicNoticeRequestRepository,
        publicNoticeDocumentRepository,
        publicNoticeDocumentLinkRepository,
        camundaWorkflowService,
        clock, notifyService, emailCaseLinkService, teamService, pwaAppProcessingPermissionService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }

  @Test
  public void canShowInTaskList_editConsentDocumentPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_notSatisfactory() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication()));

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName());
    assertThat(taskListEntry.getRoute()).isNull();
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.CANNOT_START_YET));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  public void getTaskListEntry_publicNoticeNotStarted() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(detail.getPwaApplication()));

    var taskListEntry = publicNoticeService.getTaskListEntry(PwaAppProcessingTask.PUBLIC_NOTICE, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.PUBLIC_NOTICE.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }



  @Test
  public void mapPublicNoticeDraftToForm_publicNoticeRequestExists() {

    var coverLetterTemplateText = "cover letter text...";
    when(templateTextService.getLatestVersionTextByType(TemplateTextType.PUBLIC_NOTICE_COVER_LETTER)).thenReturn(coverLetterTemplateText);

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.of(publicNotice));

    var expectedForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm();
    var publicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice, expectedForm);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(publicNotice)).thenReturn(Optional.of(publicNoticeRequest));


    var actualForm = new PublicNoticeDraftForm();
    publicNoticeService.mapPublicNoticeDraftToForm(pwaApplication, actualForm);

    assertThat(actualForm.getCoverLetterText()).isEqualTo(expectedForm.getCoverLetterText());
    assertThat(actualForm.getReason()).isEqualTo(expectedForm.getReason());
    assertThat(actualForm.getReasonDescription()).isEqualTo(expectedForm.getReasonDescription());
    verify(appFileService, times(1)).mapFilesToForm(expectedForm, pwaApplication, FILE_PURPOSE);
  }

  @Test
  public void mapPublicNoticeDraftToForm_publicNoticeRequestDoesNotExist() {

    var coverLetterTemplateText = "cover letter text...";
    when(templateTextService.getLatestVersionTextByType(TemplateTextType.PUBLIC_NOTICE_COVER_LETTER)).thenReturn(coverLetterTemplateText);

    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.empty());

    var actualForm = new PublicNoticeDraftForm();
    publicNoticeService.mapPublicNoticeDraftToForm(pwaApplication, actualForm);

    assertThat(actualForm.getCoverLetterText()).isEqualTo(coverLetterTemplateText);
    assertThat(actualForm.getReason()).isNull();
    assertThat(actualForm.getReasonDescription()).isNull();
    verifyNoInteractions(appFileService);
    verifyNoInteractions(publicNoticeRequestRepository);
  }



  @Test
  public void createPublicNoticeAndStartWorkflow_newEntities_firstVersions() {

    when(publicNoticeRepository.findFirstByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(Optional.empty());
    var expectedPublicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.save(expectedPublicNotice)).thenReturn(expectedPublicNotice);

    var expectedPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(expectedPublicNotice);
    when(publicNoticeDocumentRepository.save(expectedPublicNoticeDocument)).thenReturn(expectedPublicNoticeDocument);

    var publicNoticeAppFile = PublicNoticeTestUtil.createAppFileForPublicNotice(pwaApplication);
    var uploadFileWithDescriptionForm = new UploadFileWithDescriptionForm(
        publicNoticeAppFile.getFileId(), "desc", clock.instant());
    when(appFileService.getAppFileByPwaApplicationAndFileId(pwaApplication, publicNoticeAppFile.getFileId()))
        .thenReturn(publicNoticeAppFile);

    when(publicNoticeRepository.save(expectedPublicNotice)).thenReturn(expectedPublicNotice);

    String caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
    var regulatorTeam = TeamTestingUtils.getRegulatorTeam();
    when(teamService.getRegulatorTeam()).thenReturn(regulatorTeam);
    var regulatorTeamMember = TeamTestingUtils.createRegulatorTeamMember(
            regulatorTeam, PersonTestUtil.createDefaultPerson(), Set.of(PwaRegulatorRole.PWA_MANAGER));
    var regulatorTeamMembers = List.of(regulatorTeamMember);
    when(teamService.getTeamMembers(regulatorTeam)).thenReturn(regulatorTeamMembers);


    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(uploadFileWithDescriptionForm));
    publicNoticeService.createPublicNoticeAndStartWorkflow(publicNoticeDraftForm, pwaApplication, user);


    verify(publicNoticeRepository, times(1)).save(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice).isEqualTo(expectedPublicNotice);

    verify(publicNoticeDocumentRepository, times(1)).save(publicNoticeDocumentArgumentCaptor.capture());
    var actualPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocument).isEqualTo(expectedPublicNoticeDocument);

    verify(publicNoticeDocumentLinkRepository, times(1)).save(publicNoticeDocumentLinkArgumentCaptor.capture());
    var actualPublicNoticeDocumentLink = publicNoticeDocumentLinkArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocumentLink.getPublicNoticeDocument()).isEqualTo(actualPublicNoticeDocument);
    assertThat(actualPublicNoticeDocumentLink.getAppFile()).isEqualTo(publicNoticeAppFile);

    verify(publicNoticeRequestRepository, times(1)).save(publicNoticeRequestArgumentCaptor.capture());
    var publicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(publicNoticeRequest.getPublicNotice()).isEqualTo(actualPublicNotice);
    assertThat(publicNoticeRequest.getCoverLetterText()).isEqualTo(publicNoticeDraftForm.getCoverLetterText());
    assertThat(publicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    assertThat(publicNoticeRequest.getReason()).isEqualTo(publicNoticeDraftForm.getReason());
    assertThat(publicNoticeRequest.getReasonDescription()).isEqualTo(publicNoticeDraftForm.getReasonDescription());
    assertThat(publicNoticeRequest.getVersion()).isEqualTo(1);
    assertThat(publicNoticeRequest.getSubmittedTimestamp()).isEqualTo(clock.instant());
    assertThat(publicNoticeRequest.getCreatedByPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());

    verify(camundaWorkflowService, times(1)).startWorkflow(expectedPublicNotice);

    regulatorTeamMembers.forEach(teamMember -> {
      var expectedEmailProps = new PublicNoticeApprovalRequestEmailProps(
      teamMember.getPerson().getFullName(),
      pwaApplication.getAppReference(),
      publicNoticeDraftForm.getReason().getReasonText(),
      caseManagementLink);

      verify(notifyService, times(1)).sendEmail(expectedEmailProps, teamMember.getPerson().getEmailAddress());
    });
  }


  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndNullStatus() {

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(
        new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(null, user, pwaApplicationDetail);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.NEW_DRAFT);
  }

  @Test
  public void getAvailablePublicNoticeActions_draftPermissionAndDraftStatus() {

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(
        new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.DRAFT, user, pwaApplicationDetail);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.UPDATE_DRAFT);
  }

  @Test
  public void getAvailablePublicNoticeActions_approvePermissionAndApprovalStatus() {

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(
        new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.APPROVE_PUBLIC_NOTICE)));
    var publicNoticeActions = publicNoticeService.getAvailablePublicNoticeActions(PublicNoticeStatus.MANAGER_APPROVAL, user, pwaApplicationDetail);

    assertThat(publicNoticeActions).containsOnly(PublicNoticeAction.APPROVE);
  }

  @Test
  public void getAllPublicNoticeViews_noPublicNotices() {

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(
        new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)));

    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(pwaApplicationDetail, user);
    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isNull();
    assertThat(allPublicNoticesView.getHistoricalPublicNotices()).isEmpty();
    assertThat(allPublicNoticesView.getActions()).containsOnly(PublicNoticeAction.NEW_DRAFT);
  }

  @Test
  public void getAllPublicNoticeViews_currentPublicNoticeAndEndedPublicNotices() {

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(
        new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)));

    var currentPublicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    var endedPublicNotice1 = PublicNoticeTestUtil.createEndedPublicNotice(pwaApplication);
    var endedPublicNotice2 = PublicNoticeTestUtil.createEndedPublicNotice(pwaApplication);
    when(publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        List.of(currentPublicNotice, endedPublicNotice1 ,endedPublicNotice2));

    var currentPublicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(currentPublicNotice);
    var endedPublicNotice1Request = PublicNoticeTestUtil.createInitialPublicNoticeRequest(endedPublicNotice1);
    var endedPublicNotice2Request = PublicNoticeTestUtil.createInitialPublicNoticeRequest(endedPublicNotice2);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(currentPublicNotice))
        .thenReturn(Optional.of(currentPublicNoticeRequest));
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(endedPublicNotice1))
        .thenReturn(Optional.of(endedPublicNotice1Request));
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(endedPublicNotice2))
        .thenReturn(Optional.of(endedPublicNotice2Request));


    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(pwaApplicationDetail, user);

    var expectedCurrentPublicNoticeView = PublicNoticeTestUtil.createPublicNoticeView(currentPublicNotice, currentPublicNoticeRequest);
    var expectedEndedPublicNotice1View = PublicNoticeTestUtil.createPublicNoticeView(endedPublicNotice1, endedPublicNotice1Request);
    var expectedEndedPublicNotice2View = PublicNoticeTestUtil.createPublicNoticeView(endedPublicNotice2, endedPublicNotice2Request);

    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isEqualTo(expectedCurrentPublicNoticeView);
    assertThat(allPublicNoticesView.getHistoricalPublicNotices()).containsOnly(expectedEndedPublicNotice1View ,expectedEndedPublicNotice2View);
    assertThat(allPublicNoticesView.getActions()).containsOnly(PublicNoticeAction.UPDATE_DRAFT);
  }

  @Test
  public void getAllPublicNoticeViews_currentPublicNoticeOnly() {

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(
        new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)));

    var currentPublicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(List.of(currentPublicNotice));

    var currentPublicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(currentPublicNotice);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(currentPublicNotice))
        .thenReturn(Optional.of(currentPublicNoticeRequest));


    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(pwaApplicationDetail, user);

    var expectedCurrentPublicNoticeView = PublicNoticeTestUtil.createPublicNoticeView(currentPublicNotice, currentPublicNoticeRequest);

    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isEqualTo(expectedCurrentPublicNoticeView);
    assertThat(allPublicNoticesView.getHistoricalPublicNotices()).isEmpty();
    assertThat(allPublicNoticesView.getActions()).containsOnly(PublicNoticeAction.UPDATE_DRAFT);
  }

  @Test
  public void getAllPublicNoticeViews_onlyEndedPublicNotices() {

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(
        new ProcessingPermissionsDto(null, Set.of(PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE)));

    var endedPublicNotice1 = PublicNoticeTestUtil.createEndedPublicNotice(pwaApplication);
    var endedPublicNotice2 = PublicNoticeTestUtil.createEndedPublicNotice(pwaApplication);
    when(publicNoticeRepository.findAllByPwaApplicationOrderByVersionDesc(pwaApplication)).thenReturn(
        List.of(endedPublicNotice1 ,endedPublicNotice2));

    var endedPublicNotice1Request = PublicNoticeTestUtil.createInitialPublicNoticeRequest(endedPublicNotice1);
    var endedPublicNotice2Request = PublicNoticeTestUtil.createInitialPublicNoticeRequest(endedPublicNotice2);
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(endedPublicNotice1))
        .thenReturn(Optional.of(endedPublicNotice1Request));
    when(publicNoticeRequestRepository.findFirstByPublicNoticeOrderByVersionDesc(endedPublicNotice2))
        .thenReturn(Optional.of(endedPublicNotice2Request));


    var allPublicNoticesView = publicNoticeService.getAllPublicNoticeViews(pwaApplicationDetail, user);

    var expectedEndedPublicNotice1View = PublicNoticeTestUtil.createPublicNoticeView(endedPublicNotice1, endedPublicNotice1Request);
    var expectedEndedPublicNotice2View = PublicNoticeTestUtil.createPublicNoticeView(endedPublicNotice2, endedPublicNotice2Request);

    assertThat(allPublicNoticesView.getCurrentPublicNotice()).isNull();
    assertThat(allPublicNoticesView.getHistoricalPublicNotices()).containsOnly(expectedEndedPublicNotice1View ,expectedEndedPublicNotice2View);
    assertThat(allPublicNoticesView.getActions()).containsOnly(PublicNoticeAction.NEW_DRAFT);
  }

  @Test
  public void validate_serviceInteractions() {
    var form = new PublicNoticeDraftForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);
  }

  @Test
  public void getPublicNoticeDocumentLink_repositoryInteraction() {
    var appFile = new AppFile();
    publicNoticeService.getPublicNoticeDocumentLink(appFile);
    verify(publicNoticeDocumentLinkRepository, times(1)).findByAppFile(appFile);
  }

  @Test
  public void deleteFileLinkAndPublicNoticeDocument_repositoryInteractions() {
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(new PublicNoticeDocument(), null);
    publicNoticeService.deleteFileLinkAndPublicNoticeDocument(publicNoticeDocumentLink);
    verify(publicNoticeDocumentRepository, times(1)).delete(publicNoticeDocumentLink.getPublicNoticeDocument());
    verify(publicNoticeDocumentLinkRepository, times(1)).delete(publicNoticeDocumentLink);
  }

  @Test
  public void publicNoticeInProgress_no() {

    var endedNotice = new PublicNotice();
    endedNotice.setStatus(PublicNoticeStatus.ENDED);

    var withdrawnNotice = new PublicNotice();
    withdrawnNotice.setStatus(PublicNoticeStatus.WITHDRAWN);

    when(publicNoticeRepository.findAllByPwaApplication(any())).thenReturn(List.of(endedNotice, withdrawnNotice));

    boolean inProgress = publicNoticeService.publicNoticeInProgress(new PwaApplication());

    assertThat(inProgress).isFalse();

  }

  @Test
  public void publicNoticeInProgress_yes() {

    var endedNotice = new PublicNotice();
    endedNotice.setStatus(PublicNoticeStatus.ENDED);

    var inProgressNotice = new PublicNotice();
    inProgressNotice.setStatus(PublicNoticeStatus.APPLICANT_UPDATE);

    when(publicNoticeRepository.findAllByPwaApplication(any())).thenReturn(List.of(endedNotice, inProgressNotice));

    boolean inProgress = publicNoticeService.publicNoticeInProgress(new PwaApplication());

    assertThat(inProgress).isTrue();

  }

}
