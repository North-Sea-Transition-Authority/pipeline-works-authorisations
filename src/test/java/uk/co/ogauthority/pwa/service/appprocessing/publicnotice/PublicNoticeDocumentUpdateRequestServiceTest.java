package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDocumentUpdateRequestForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeUpdateRequestEmailProps;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationPublicNoticeDocumentResult;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDocumentUpdateRequestValidator;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeDocumentUpdateRequestServiceTest {

  private PublicNoticeDocumentUpdateRequestService publicNoticeDocumentUpdateRequestService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private PublicNoticeDocumentUpdateRequestValidator validator;

  @Mock
  private PublicNoticeDocumentRepository publicNoticeDocumentRepository;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private NotifyService notifyService;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocument> publicNoticeDocumentArgumentCaptor;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;



  @Before
  public void setUp() {

    publicNoticeDocumentUpdateRequestService = new PublicNoticeDocumentUpdateRequestService(publicNoticeService, validator,
        publicNoticeDocumentRepository, camundaWorkflowService, emailCaseLinkService, pwaContactService, notifyService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
  }


  @Test
  public void publicNoticeDocumentUpdateCanBeRequested_updatablePublicNoticeExistsForApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateRequestService.publicNoticeDocumentUpdateCanBeRequested(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  public void publicNoticeDocumentUpdateCanBeRequested_updatablePublicNoticeExistsForDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateRequestService.publicNoticeDocumentUpdateCanBeRequested(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void publicNoticeDocumentUpdateCanBeRequested_updatablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of());
    var publicNoticeExists = publicNoticeDocumentUpdateRequestService.publicNoticeDocumentUpdateCanBeRequested(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }


  @Test
  public void validate_verifyServiceInteractions() {
    var form = new PublicNoticeDocumentUpdateRequestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeDocumentUpdateRequestService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);
  }


  @Test
  public void updatePublicNoticeDocumentAndTransitionWorkflow_publicNoticeAndDocumentIsUpdated_workflowTransitioned() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenReturn(latestPublicNoticeDocument);
    var newPublicNotice = PublicNoticeTestUtil.createApplicantUpdatePublicNotice(pwaApplication);
    when(publicNoticeService.savePublicNotice(newPublicNotice)).thenReturn(newPublicNotice);

    var form = new PublicNoticeDocumentUpdateRequestForm("some comments");
    publicNoticeDocumentUpdateRequestService.updatePublicNoticeDocumentAndTransitionWorkflow(pwaApplication, form);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(
        publicNotice, PwaApplicationPublicNoticeDocumentResult.UPDATE_REQUESTED);
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW));

    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.APPLICANT_UPDATE);

    verify(publicNoticeDocumentRepository, times(1)).save(publicNoticeDocumentArgumentCaptor.capture());
    var actualNewPublicNoticeDocument = publicNoticeDocumentArgumentCaptor.getValue();
    assertThat(actualNewPublicNoticeDocument.getComments()).isEqualTo(form.getComments());
  }


  @Test
  public void updatePublicNoticeDocumentAndTransitionWorkflow_emailSentToApplicant() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenReturn(latestPublicNoticeDocument);
    var newPublicNotice = PublicNoticeTestUtil.createApplicantUpdatePublicNotice(pwaApplication);
    when(publicNoticeService.savePublicNotice(newPublicNotice)).thenReturn(newPublicNotice);

    String caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
    var emailRecipients = List.of(PersonTestUtil.createDefaultPerson());
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplication, PwaContactRole.PREPARER))
        .thenReturn(emailRecipients);

    var form = new PublicNoticeDocumentUpdateRequestForm("some comments");
    publicNoticeDocumentUpdateRequestService.updatePublicNoticeDocumentAndTransitionWorkflow(pwaApplication, form);

    emailRecipients.forEach(recipient -> {
      var expectedEmailProps = new PublicNoticeUpdateRequestEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          form.getComments(),
          caseManagementLink);

      verify(notifyService, times(1)).sendEmail(expectedEmailProps, recipient.getEmailAddress());
    });
  }



}
