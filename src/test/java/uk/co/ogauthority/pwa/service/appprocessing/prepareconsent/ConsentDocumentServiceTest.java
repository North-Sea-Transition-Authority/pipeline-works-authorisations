package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.documents.view.SectionView;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsentDocumentServiceTest {

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private ConsentDocumentEmailService consentDocumentEmailService;

  @Mock
  private ConsentReviewService consentReviewService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  private ConsentDocumentService consentDocumentService;

  private PwaApplicationDetail detail;

  private final Person person = PersonTestUtil.createDefaultPerson();
  private final AuthenticatedUserAccount authUser = new AuthenticatedUserAccount(new WebUserAccount(1, person), Set.of());

  @Before
  public void setUp() throws Exception {

    consentDocumentService = new ConsentDocumentService(
        applicationUpdateRequestService,
        consultationRequestService,
        publicNoticeService,
        documentInstanceService,
        pwaApplicationDetailService,
        consentDocumentEmailService,
        consentReviewService,
        camundaWorkflowService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  public void canSendForApproval_latestVersionNotSatisfactory() {

    detail.setConfirmedSatisfactoryTimestamp(null);

    boolean canSend = consentDocumentService.canSendForApproval(detail);
    assertThat(canSend).isFalse();

  }

  @Test
  public void canSendForApproval_latestVersionSatisfactory_updateInProgress() {

    detail.setConfirmedSatisfactoryTimestamp(Instant.now());
    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail)).thenReturn(true);

    boolean canSend = consentDocumentService.canSendForApproval(detail);
    assertThat(canSend).isFalse();

  }

  @Test
  public void canSendForApproval_latestVersionSatisfactory_noUpdateInProgress_consultationInProgress() {

    detail.setConfirmedSatisfactoryTimestamp(Instant.now());
    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail)).thenReturn(false);
    when(consultationRequestService.getAllOpenRequestsByApplication(detail.getPwaApplication()))
        .thenReturn(List.of(new ConsultationRequest()));

    boolean canSend = consentDocumentService.canSendForApproval(detail);
    assertThat(canSend).isFalse();

  }

  @Test
  public void canSendForApproval_latestVersionSatisfactory_noUpdateInProgress_noConsultationInProgress_publicNoticeInProgress() {

    detail.setConfirmedSatisfactoryTimestamp(Instant.now());
    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail)).thenReturn(false);
    when(consultationRequestService.getAllOpenRequestsByApplication(detail.getPwaApplication()))
        .thenReturn(List.of());
    when(publicNoticeService.publicNoticeInProgress(detail.getPwaApplication())).thenReturn(true);

    boolean canSend = consentDocumentService.canSendForApproval(detail);
    assertThat(canSend).isFalse();

  }

  @Test
  public void canSendForApproval_latestVersionSatisfactory_noUpdateInProgress_noConsultationInProgress_noPublicNoticeInProgress_noDocumentClauses() {

    detail.setConfirmedSatisfactoryTimestamp(Instant.now());
    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail)).thenReturn(false);
    when(consultationRequestService.getAllOpenRequestsByApplication(detail.getPwaApplication()))
        .thenReturn(List.of());
    when(publicNoticeService.publicNoticeInProgress(detail.getPwaApplication())).thenReturn(false);

    var instance = new DocumentInstance();
    when(documentInstanceService.getDocumentInstance(detail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT))
        .thenReturn(Optional.of(instance));

    var emptyDocView = new DocumentView(PwaDocumentType.INSTANCE, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);
    emptyDocView.setSections(List.of(new SectionView()));
    when(documentInstanceService.getDocumentView(instance)).thenReturn(emptyDocView);

    boolean canSend = consentDocumentService.canSendForApproval(detail);
    assertThat(canSend).isFalse();

  }

  @Test
  public void canSendForApproval_latestVersionSatisfactory_noUpdateInProgress_noConsultationInProgress_noPublicNoticeInProgress_documentHasClauses() {

    detail.setConfirmedSatisfactoryTimestamp(Instant.now());
    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail)).thenReturn(false);
    when(consultationRequestService.getAllOpenRequestsByApplication(detail.getPwaApplication()))
        .thenReturn(List.of());
    when(publicNoticeService.publicNoticeInProgress(detail.getPwaApplication())).thenReturn(false);

    var instance = new DocumentInstance();
    when(documentInstanceService.getDocumentInstance(detail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT))
        .thenReturn(Optional.of(instance));

    var docView = new DocumentView(PwaDocumentType.INSTANCE, DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);
    var sectionView = new SectionView();
    sectionView.setClauses(List.of(new SectionClauseVersionView()));
    docView.setSections(List.of(sectionView));
    when(documentInstanceService.getDocumentView(instance)).thenReturn(docView);

    boolean canSend = consentDocumentService.canSendForApproval(detail);
    assertThat(canSend).isTrue();

  }

  @Test
  public void sendForApproval_verifyServiceCalls() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    consentDocumentService.sendForApproval(detail, "cover letter my text", authUser);

    verify(pwaApplicationDetailService, times(1)).updateStatus(detail, PwaApplicationStatus.CONSENT_REVIEW, authUser);

    verify(consentReviewService, times(1)).startConsentReview(detail, "cover letter my text", person);

    verify(consentDocumentEmailService, times(1)).sendConsentReviewStartedEmail(detail, person);

    var workflowTaskInstance = new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);
    verify(camundaWorkflowService, times(1)).completeTask(workflowTaskInstance);

  }

}