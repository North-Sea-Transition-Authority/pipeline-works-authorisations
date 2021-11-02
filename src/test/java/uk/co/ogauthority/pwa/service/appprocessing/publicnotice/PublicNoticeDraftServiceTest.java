package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeApprovalRequestEmailProps;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeDraftServiceTest {

  private PublicNoticeDraftService publicNoticeDraftService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private AppFileService appFileService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private Clock clock;

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private PwaTeamService pwaTeamService;

  @Captor
  private ArgumentCaptor<PublicNoticeApprovalRequestEmailProps> approvalRequestEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<String> emailAddressCaptor;

  private PwaApplication pwaApplication;
  private AuthenticatedUserAccount user;

  private static String FILE_ID = "1";

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

    publicNoticeDraftService = new PublicNoticeDraftService(
        appFileService,
        publicNoticeService,
        camundaWorkflowService,
        clock, notifyService, emailCaseLinkService, pwaTeamService);

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }




  @Test
  public void submitPublicNoticeDraft_noPublicNoticeExists_newEntitiesCreatedAndSaved() {

    when(publicNoticeService.getLatestPublicNoticeOpt(pwaApplication)).thenReturn(Optional.empty());
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);

    var expectedPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.savePublicNoticeDocument(expectedPublicNoticeDocument)).thenReturn(expectedPublicNoticeDocument);

    var uploadFileWithDescriptionForm = new UploadFileWithDescriptionForm(
        FILE_ID, "desc", clock.instant());
    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(uploadFileWithDescriptionForm));
    var publicNoticeAppFile = new AppFile();
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(expectedPublicNoticeDocument, publicNoticeAppFile);
    when(publicNoticeService.createPublicNoticeDocumentLinkFromForm(pwaApplication, uploadFileWithDescriptionForm, expectedPublicNoticeDocument))
    .thenReturn(publicNoticeDocumentLink);


    publicNoticeDraftService.submitPublicNoticeDraft(publicNoticeDraftForm, pwaApplication, user);


    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice).isEqualTo(publicNotice);

    verify(publicNoticeService, times(1)).savePublicNoticeDocument(publicNoticeDocumentArgumentCaptor.capture());
    var actualPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocument).isEqualTo(expectedPublicNoticeDocument);

    verify(publicNoticeService, times(1)).savePublicNoticeDocumentLink(publicNoticeDocumentLinkArgumentCaptor.capture());
    var actualPublicNoticeDocumentLink = publicNoticeDocumentLinkArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocumentLink.getPublicNoticeDocument()).isEqualTo(actualPublicNoticeDocument);
    assertThat(actualPublicNoticeDocumentLink.getAppFile()).isEqualTo(publicNoticeAppFile);

    verify(publicNoticeService, times(1)).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
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
  public void submitPublicNoticeDraft_emailSent() {

    when(publicNoticeService.getLatestPublicNoticeOpt(pwaApplication)).thenReturn(Optional.empty());
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);

    var expectedPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.savePublicNoticeDocument(expectedPublicNoticeDocument)).thenReturn(expectedPublicNoticeDocument);

    String caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);

    var pwaManager1 = PersonTestUtil.createPersonFrom(new PersonId(1));
    var pwaManager2 = PersonTestUtil.createPersonFrom(new PersonId(2));
    var pwaManagers = Set.of(pwaManager1, pwaManager2);
    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER)).thenReturn(pwaManagers);

    var uploadFileWithDescriptionForm = new UploadFileWithDescriptionForm(
        FILE_ID, "desc", clock.instant());
    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(uploadFileWithDescriptionForm));
    var publicNoticeAppFile = new AppFile();
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(expectedPublicNoticeDocument, publicNoticeAppFile);
    when(publicNoticeService.createPublicNoticeDocumentLinkFromForm(pwaApplication, uploadFileWithDescriptionForm, expectedPublicNoticeDocument))
    .thenReturn(publicNoticeDocumentLink);


    publicNoticeDraftService.submitPublicNoticeDraft(publicNoticeDraftForm, pwaApplication, user);

    verify(notifyService, times(pwaManagers.size())).sendEmail(approvalRequestEmailPropsCaptor.capture(), emailAddressCaptor.capture());

    assertThat(approvalRequestEmailPropsCaptor.getAllValues()).allSatisfy(emailProps -> {

      assertThat(emailProps.getEmailPersonalisation()).contains(
          entry("APPLICATION_REFERENCE", pwaApplication.getAppReference()),
          entry("PUBLIC_NOTICE_REASON", publicNoticeDraftForm.getReason().getReasonText()),
          entry("CASE_MANAGEMENT_LINK", caseManagementLink)
      );

      assertThat(emailProps.getTemplate()).isEqualTo(NotifyTemplate.PUBLIC_NOTICE_APPROVAL_REQUEST);

    });

    assertThat(approvalRequestEmailPropsCaptor.getAllValues().get(0).getRecipientFullName()).isEqualTo(pwaManager1.getFullName());
    assertThat(emailAddressCaptor.getAllValues().get(0)).isEqualTo(pwaManager1.getEmailAddress());

    assertThat(approvalRequestEmailPropsCaptor.getAllValues().get(1).getRecipientFullName()).isEqualTo(pwaManager2.getFullName());
    assertThat(emailAddressCaptor.getAllValues().get(1)).isEqualTo(pwaManager2.getEmailAddress());
  }


  @Test
  public void submitPublicNoticeDraft_firstDraftRejected_submittingSecondDraft_entitiesUpdatedAndSaved() {

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
    when(publicNoticeService.savePublicNoticeDocument(newPublicNoticeDocument)).thenReturn(newPublicNoticeDocument);

    var uploadFileWithDescriptionForm = new UploadFileWithDescriptionForm(
        FILE_ID, "desc", clock.instant());
    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(uploadFileWithDescriptionForm));
    var publicNoticeAppFile = new AppFile();
    var publicNoticeDocumentLink = new PublicNoticeDocumentLink(newPublicNoticeDocument, publicNoticeAppFile);
    when(publicNoticeService.createPublicNoticeDocumentLinkFromForm(pwaApplication, uploadFileWithDescriptionForm, newPublicNoticeDocument))
        .thenReturn(publicNoticeDocumentLink);


    publicNoticeDraftService.submitPublicNoticeDraft(publicNoticeDraftForm, pwaApplication, user);


    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice).isEqualTo(publicNotice);

    verify(publicNoticeService, times(2)).savePublicNoticeDocument(publicNoticeDocumentArgumentCaptor.capture());
    var actualLatestPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getAllValues().get(0);
    assertThat(actualLatestPublicNoticeDocument).isEqualTo(latestPublicNoticeDocument);
    var actualNewPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getAllValues().get(1);
    assertThat(actualNewPublicNoticeDocument).isEqualTo(newPublicNoticeDocument);

    verify(publicNoticeService, times(1)).savePublicNoticeDocumentLink(publicNoticeDocumentLinkArgumentCaptor.capture());
    var actualPublicNoticeDocumentLink = publicNoticeDocumentLinkArgumentCaptor.getValue();
    assertThat(actualPublicNoticeDocumentLink.getPublicNoticeDocument()).isEqualTo(actualNewPublicNoticeDocument);
    assertThat(actualPublicNoticeDocumentLink.getAppFile()).isEqualTo(publicNoticeAppFile);

    verify(publicNoticeService, times(1)).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var publicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(publicNoticeRequest.getPublicNotice()).isEqualTo(actualPublicNotice);
    assertThat(publicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    assertThat(publicNoticeRequest.getVersion()).isEqualTo(latestPublicNoticeRequest.getVersion() + 1);

    verify(camundaWorkflowService).completeTask(new WorkflowTaskInstance(publicNotice, PwaApplicationPublicNoticeWorkflowTask.DRAFT));
  }



  @Test
  public void submitPublicNoticeDraft_firstVersionEnded_submittingSecondVersion_firstDraft() {

    var publicNotice = PublicNoticeTestUtil.createEndedPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNoticeOpt(pwaApplication)).thenReturn(Optional.of(publicNotice));
    publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    publicNotice.setVersion(2);

    when(publicNoticeService.isPublicNoticeStatusEnded(PublicNoticeStatus.ENDED)).thenReturn(true);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createArchivedPublicNoticeDocument(publicNotice);
    var newPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    newPublicNoticeDocument.setVersion(latestPublicNoticeDocument.getVersion() + 1);

    var uploadFileWithDescriptionForm = new UploadFileWithDescriptionForm(
        FILE_ID, "desc", clock.instant());
    var publicNoticeDraftForm = PublicNoticeTestUtil.createDefaultPublicNoticeDraftForm(List.of(uploadFileWithDescriptionForm));

    publicNoticeDraftService.submitPublicNoticeDraft(publicNoticeDraftForm, pwaApplication, user);


    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice).isEqualTo(publicNotice);

    verify(publicNoticeService, times(1)).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var publicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(publicNoticeRequest.getPublicNotice()).isEqualTo(actualPublicNotice);
    assertThat(publicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.WAITING_MANAGER_APPROVAL);
    assertThat(publicNoticeRequest.getVersion()).isEqualTo(1);

    verify(camundaWorkflowService).startWorkflow(publicNotice);
  }


}
