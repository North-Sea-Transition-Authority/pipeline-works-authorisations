package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeApprovalRequestEmailProps;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PublicNoticeDraftServiceTest {

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private Clock clock;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private PublicNoticeDraftService publicNoticeDraftService;

  @Mock
  private AppFileManagementService appFileManagementService;

  @Captor
  private ArgumentCaptor<PublicNoticeApprovalRequestEmailProps> approvalRequestEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientArgumentCaptor;

  private PwaApplication pwaApplication;
  private AuthenticatedUserAccount user;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocument> publicNoticeDocumentArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocumentLink> publicNoticeDocumentLinkArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeRequest> publicNoticeRequestArgumentCaptor;

  @BeforeEach
  void setUp() {
    when(clock.instant()).thenReturn(Instant.now());

    publicNoticeDraftService = new PublicNoticeDraftService(
        publicNoticeService,
        camundaWorkflowService,
        clock,
        caseLinkService,
        teamQueryService,
        appFileManagementService,
        emailService
    );

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  void submitPublicNoticeDraft_noPublicNoticeExists_newEntitiesCreatedAndSaved() {

    when(publicNoticeService.getLatestPublicNoticeOpt(pwaApplication)).thenReturn(Optional.empty());
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);

    var expectedPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.savePublicNoticeDocument(expectedPublicNoticeDocument)).thenReturn(expectedPublicNoticeDocument);

    var fileForm = FileManagementValidatorTestUtils.createUploadedFileForm();
    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(fileForm));
    var publicNoticeAppFile = new AppFile();
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(expectedPublicNoticeDocument, publicNoticeAppFile);
    when(publicNoticeService.createPublicNoticeDocumentLinkFromFileId(pwaApplication, String.valueOf(fileForm.getFileId()), expectedPublicNoticeDocument))
    .thenReturn(publicNoticeDocumentLink);


    publicNoticeDraftService.submitPublicNoticeDraft(publicNoticeDraftForm, pwaApplication, user);


    verify(publicNoticeService).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice).isEqualTo(publicNotice);

    verify(publicNoticeService).savePublicNoticeDocument(publicNoticeDocumentArgumentCaptor.capture());
    var actualPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocument).isEqualTo(expectedPublicNoticeDocument);

    verify(publicNoticeService).savePublicNoticeDocumentLink(publicNoticeDocumentLinkArgumentCaptor.capture());
    var actualPublicNoticeDocumentLink = publicNoticeDocumentLinkArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocumentLink.getPublicNoticeDocument()).isEqualTo(actualPublicNoticeDocument);
    assertThat(actualPublicNoticeDocumentLink.getAppFile()).isEqualTo(publicNoticeAppFile);

    verify(publicNoticeService).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var publicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(publicNoticeRequest.getPublicNotice()).isEqualTo(actualPublicNotice);
    assertThat(publicNoticeRequest.getCoverLetterText()).isEqualTo(publicNoticeDraftForm.getCoverLetterText());
    assertThat(publicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    assertThat(publicNoticeRequest.getReason()).isEqualTo(publicNoticeDraftForm.getReason());
    assertThat(publicNoticeRequest.getReasonDescription()).isEqualTo(publicNoticeDraftForm.getReasonDescription());
    assertThat(publicNoticeRequest.getVersion()).isEqualTo(1);
    assertThat(publicNoticeRequest.getCreatedTimestamp()).isEqualTo(clock.instant());
    assertThat(publicNoticeRequest.getCreatedByPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());

    verify(camundaWorkflowService).startWorkflow(publicNotice);
  }


  @Test
  void submitPublicNoticeDraft_emailSent() {

    when(publicNoticeService.getLatestPublicNoticeOpt(pwaApplication)).thenReturn(Optional.empty());
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);

    var expectedPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.savePublicNoticeDocument(expectedPublicNoticeDocument)).thenReturn(expectedPublicNoticeDocument);

    String caseManagementLink = "case management link url";
    when(caseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);

    var pwaManager1 = new TeamMemberView(1L, "Mr.", "PWA", "Manager1", "manager1@pwa.co.uk", null, null, null);
    var pwaManager2 = new TeamMemberView(2L, "Ms.", "PWA", "Manager2", "manager2@pwa.co.uk", null, null, null);
    List<TeamMemberView> pwaManagers = List.of(pwaManager1, pwaManager2);
    when(teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, Role.PWA_MANAGER)).thenReturn(pwaManagers);

    var fileForm = FileManagementValidatorTestUtils.createUploadedFileForm();
    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(fileForm));
    var publicNoticeAppFile = new AppFile();
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(expectedPublicNoticeDocument, publicNoticeAppFile);
    when(publicNoticeService.createPublicNoticeDocumentLinkFromFileId(pwaApplication, String.valueOf(fileForm.getFileId()), expectedPublicNoticeDocument))
    .thenReturn(publicNoticeDocumentLink);


    publicNoticeDraftService.submitPublicNoticeDraft(publicNoticeDraftForm, pwaApplication, user);

    verify(emailService, times(pwaManagers.size())).sendEmail(approvalRequestEmailPropsCaptor.capture(), emailRecipientArgumentCaptor.capture(), eq(pwaApplication.getAppReference()));

    assertThat(approvalRequestEmailPropsCaptor.getAllValues()).allSatisfy(emailProps -> {

      assertThat(emailProps.getEmailPersonalisation()).contains(
          entry("APPLICATION_REFERENCE", pwaApplication.getAppReference()),
          entry("PUBLIC_NOTICE_REASON", publicNoticeDraftForm.getReason().getReasonText()),
          entry("CASE_MANAGEMENT_LINK", caseManagementLink)
      );

      assertThat(emailProps.getTemplate()).isEqualTo(NotifyTemplate.PUBLIC_NOTICE_APPROVAL_REQUEST);

    });

    assertThat(approvalRequestEmailPropsCaptor.getAllValues().get(0).getRecipientFullName()).isEqualTo(pwaManager1.getFullName());
    assertThat(emailRecipientArgumentCaptor.getAllValues().get(0).getEmailAddress()).isEqualTo(pwaManager1.email());

    assertThat(approvalRequestEmailPropsCaptor.getAllValues().get(1).getRecipientFullName()).isEqualTo(pwaManager2.getFullName());
    assertThat(emailRecipientArgumentCaptor.getAllValues().get(1).getEmailAddress()).isEqualTo(pwaManager2.email());
  }


  @Test
  void submitPublicNoticeDraft_firstDraftRejected_submittingSecondDraft_entitiesUpdatedAndSaved() {

    var publicNotice = PublicNoticeTestUtil.createDraftPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNoticeOpt(pwaApplication)).thenReturn(Optional.of(publicNotice));
    publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);

    var latestPublicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice)).thenReturn(latestPublicNoticeRequest);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenReturn(latestPublicNoticeDocument);
    latestPublicNoticeDocument = PublicNoticeTestUtil.createArchivedPublicNoticeDocument(publicNotice);
    var newPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    newPublicNoticeDocument.setVersion(latestPublicNoticeDocument.getVersion() + 1);

    var fileForm = FileManagementValidatorTestUtils.createUploadedFileForm();
    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(fileForm));
    var publicNoticeAppFile = new AppFile();

    when(publicNoticeService.savePublicNoticeDocument(any(PublicNoticeDocument.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(publicNoticeService.createPublicNoticeDocumentLinkFromFileId(eq(pwaApplication), anyString(), any(PublicNoticeDocument.class)))
        .thenAnswer(invocation -> new PublicNoticeDocumentLink(invocation.getArgument(2), publicNoticeAppFile));

    publicNoticeDraftService.submitPublicNoticeDraft(publicNoticeDraftForm, pwaApplication, user);

    verify(publicNoticeService).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice).isEqualTo(publicNotice);

    verify(publicNoticeService, times(2)).savePublicNoticeDocument(publicNoticeDocumentArgumentCaptor.capture());
    var actualLatestPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getAllValues().get(0);
    assertThat(actualLatestPublicNoticeDocument).isEqualTo(latestPublicNoticeDocument);
    var actualNewPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getAllValues().get(1);
    assertThat(actualNewPublicNoticeDocument).isEqualTo(newPublicNoticeDocument);

    verify(publicNoticeService).savePublicNoticeDocumentLink(publicNoticeDocumentLinkArgumentCaptor.capture());
    var actualPublicNoticeDocumentLink = publicNoticeDocumentLinkArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocumentLink.getPublicNoticeDocument()).isEqualTo(actualNewPublicNoticeDocument);
    assertThat(actualPublicNoticeDocumentLink.getAppFile()).isEqualTo(publicNoticeAppFile);

    verify(publicNoticeService).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var publicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(publicNoticeRequest.getPublicNotice()).isEqualTo(actualPublicNotice);
    assertThat(publicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    assertThat(publicNoticeRequest.getVersion()).isEqualTo(latestPublicNoticeRequest.getVersion() + 1);

    verify(camundaWorkflowService).completeTask(new WorkflowTaskInstance(publicNotice, PwaApplicationPublicNoticeWorkflowTask.DRAFT));
  }

  @Test
  void submitPublicNoticeDraft_firstDraftRejected_submittingSecondDraft_noErrorWhenNoDocAssociatedWithPublicNotice() {

    var publicNotice = PublicNoticeTestUtil.createDraftPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNoticeOpt(pwaApplication)).thenReturn(Optional.of(publicNotice));
    publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);

    var latestPublicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice)).thenReturn(latestPublicNoticeRequest);

    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenThrow(EntityLatestVersionNotFoundException.class);

    var newPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    newPublicNoticeDocument.setVersion(1);
    when(publicNoticeService.savePublicNoticeDocument(newPublicNoticeDocument)).thenReturn(newPublicNoticeDocument);

    var fileForm = FileManagementValidatorTestUtils.createUploadedFileForm();
    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(fileForm));
    var publicNoticeAppFile = new AppFile();
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(newPublicNoticeDocument, publicNoticeAppFile);
    when(publicNoticeService.createPublicNoticeDocumentLinkFromFileId(pwaApplication, String.valueOf(fileForm.getFileId()), newPublicNoticeDocument))
        .thenReturn(publicNoticeDocumentLink);

    publicNoticeDraftService.submitPublicNoticeDraft(publicNoticeDraftForm, pwaApplication, user);

    verify(publicNoticeService).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice).isEqualTo(publicNotice);

    verify(publicNoticeService).savePublicNoticeDocument(publicNoticeDocumentArgumentCaptor.capture());
    var actualNewPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getValue();
    assertThat(actualNewPublicNoticeDocument).isEqualTo(newPublicNoticeDocument);

    verify(publicNoticeService).savePublicNoticeDocumentLink(publicNoticeDocumentLinkArgumentCaptor.capture());
    var actualPublicNoticeDocumentLink = publicNoticeDocumentLinkArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocumentLink.getPublicNoticeDocument()).isEqualTo(actualNewPublicNoticeDocument);
    assertThat(actualPublicNoticeDocumentLink.getAppFile()).isEqualTo(publicNoticeAppFile);

    verify(publicNoticeService).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var publicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(publicNoticeRequest.getPublicNotice()).isEqualTo(actualPublicNotice);
    assertThat(publicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    assertThat(publicNoticeRequest.getVersion()).isEqualTo(latestPublicNoticeRequest.getVersion() + 1);

    verify(camundaWorkflowService).completeTask(new WorkflowTaskInstance(publicNotice, PwaApplicationPublicNoticeWorkflowTask.DRAFT));

  }

  @Test
  void submitPublicNoticeDraft_firstVersionEnded_submittingSecondVersion_firstDraft() {

    var publicNotice = PublicNoticeTestUtil.createEndedPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNoticeOpt(pwaApplication)).thenReturn(Optional.of(publicNotice));
    publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    publicNotice.setVersion(2);

    when(publicNoticeService.isPublicNoticeStatusEnded(PublicNoticeStatus.ENDED)).thenReturn(true);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createArchivedPublicNoticeDocument(publicNotice);
    var newPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    newPublicNoticeDocument.setVersion(latestPublicNoticeDocument.getVersion() + 1);

    var fileForm = FileManagementValidatorTestUtils.createUploadedFileForm();
    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(fileForm));

    publicNoticeDraftService.submitPublicNoticeDraft(publicNoticeDraftForm, pwaApplication, user);


    verify(publicNoticeService).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice).isEqualTo(publicNotice);

    verify(publicNoticeService).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var publicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(publicNoticeRequest.getPublicNotice()).isEqualTo(actualPublicNotice);
    assertThat(publicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    assertThat(publicNoticeRequest.getVersion()).isEqualTo(1);

    verify(camundaWorkflowService).startWorkflow(publicNotice);
  }

}
