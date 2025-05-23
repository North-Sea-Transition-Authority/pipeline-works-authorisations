package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeUpdateRequestEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDocumentUpdateRequestForm;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDocumentRepository;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PublicNoticeCaseOfficerReviewResult;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDocumentUpdateRequestValidator;

@ExtendWith(MockitoExtension.class)
class PublicNoticeDocumentUpdateRequestServiceTest {

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
  private CaseLinkService caseLinkService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private EmailService emailService;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDocument> publicNoticeDocumentArgumentCaptor;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;


  @BeforeEach
  void setUp() {

    publicNoticeDocumentUpdateRequestService = new PublicNoticeDocumentUpdateRequestService(publicNoticeService, validator,
        publicNoticeDocumentRepository, camundaWorkflowService, caseLinkService, pwaContactService, emailService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
  }


  @Test
  void publicNoticeDocumentUpdateCanBeRequested_updatablePublicNoticeExistsForApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateRequestService.publicNoticeDocumentUpdateCanBeRequested(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  void publicNoticeDocumentUpdateCanBeRequested_updatablePublicNoticeExistsForDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateRequestService.publicNoticeDocumentUpdateCanBeRequested(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  void publicNoticeDocumentUpdateCanBeRequested_updatablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of());
    var publicNoticeExists = publicNoticeDocumentUpdateRequestService.publicNoticeDocumentUpdateCanBeRequested(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }


  @Test
  void validate_verifyServiceInteractions() {
    var form = new PublicNoticeDocumentUpdateRequestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeDocumentUpdateRequestService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);
  }


  @Test
  void updatePublicNoticeDocumentAndTransitionWorkflow_publicNoticeAndDocumentIsUpdated_workflowTransitioned() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenReturn(latestPublicNoticeDocument);
    var newPublicNotice = PublicNoticeTestUtil.createApplicantUpdatePublicNotice(pwaApplication);
    when(publicNoticeService.savePublicNotice(newPublicNotice)).thenReturn(newPublicNotice);

    var form = new PublicNoticeDocumentUpdateRequestForm("some comments");
    publicNoticeDocumentUpdateRequestService.updatePublicNoticeDocumentAndTransitionWorkflow(pwaApplication, form);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(
        publicNotice, PublicNoticeCaseOfficerReviewResult.UPDATE_REQUESTED);
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
  void updatePublicNoticeDocumentAndTransitionWorkflow_emailSentToApplicant() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var latestPublicNoticeDocument = PublicNoticeTestUtil.createInitialPublicNoticeDocument(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeDocument(publicNotice)).thenReturn(latestPublicNoticeDocument);
    var newPublicNotice = PublicNoticeTestUtil.createApplicantUpdatePublicNotice(pwaApplication);
    when(publicNoticeService.savePublicNotice(newPublicNotice)).thenReturn(newPublicNotice);

    String caseManagementLink = "case management link url";
    when(caseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
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

      verify(emailService, times(1)).sendEmail(expectedEmailProps, recipient, pwaApplication.getAppReference());
    });
  }



}
