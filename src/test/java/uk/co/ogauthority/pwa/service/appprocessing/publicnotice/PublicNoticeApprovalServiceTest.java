package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.Assignment;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.AssignmentService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeApprovedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeRejectedEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeApprovalValidator;

@ExtendWith(MockitoExtension.class)
class PublicNoticeApprovalServiceTest {

  private PublicNoticeApprovalService publicNoticeApprovalService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private PublicNoticeApprovalValidator validator;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private Clock clock;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PersonService personService;

  @Mock
  private AssignmentService assignmentService;

  @Mock
  private EmailService emailService;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeRequest> publicNoticeRequestArgumentCaptor;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;


  @BeforeEach
  void setUp() {

    publicNoticeApprovalService = new PublicNoticeApprovalService(publicNoticeService, validator,
        camundaWorkflowService, clock, caseLinkService, pwaContactService,
        personService, assignmentService, emailService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  void openPublicNoticeCanBeApproved_approvablePublicNoticeExistsWithApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeApprovalService.openPublicNoticeCanBeApproved(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  void openPublicNoticeCanBeApproved_approvablePublicNoticeExistsWithDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeApprovalService.openPublicNoticeCanBeApproved(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  void openPublicNoticeCanBeApproved_approvablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)).thenReturn(List.of());
    var publicNoticeExists = publicNoticeApprovalService.openPublicNoticeCanBeApproved(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }


  @Test
  void updatePublicNoticeRequest_requestApproved() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var publicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice)).thenReturn(publicNoticeRequest);

    String caseManagementLink = "case management link url";
    when(caseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
    var emailRecipients = List.of(PersonTestUtil.createDefaultPerson());
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplication, PwaContactRole.PREPARER))
        .thenReturn(emailRecipients);

    var form = PublicNoticeApprovalTestUtil.createApprovedPublicNoticeForm();
    publicNoticeApprovalService.updatePublicNoticeRequest(form, pwaApplication, user);


    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.APPLICANT_UPDATE);

    verify(publicNoticeService, times(1)).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var actualPublicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(actualPublicNoticeRequest.getRequestApproved()).isTrue();
    assertThat(actualPublicNoticeRequest.getResponderPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());
    assertThat(actualPublicNoticeRequest.getResponseTimestamp()).isEqualTo(clock.instant());
    assertThat(actualPublicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.APPROVED);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(publicNotice, form.getRequestApproved());
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.MANAGER_APPROVAL));

    emailRecipients.forEach(recipient -> {
      var expectedEmailProps = new PublicNoticeApprovedEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          caseManagementLink);

      verify(emailService, times(1)).sendEmail(expectedEmailProps, recipient, pwaApplication.getAppReference());
    });
  }

  @Test
  void updatePublicNoticeRequest_requestRejected() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var publicNoticeRequest = PublicNoticeTestUtil.createInitialPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice)).thenReturn(publicNoticeRequest);

    String caseManagementLink = "case management link url";
    when(caseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);

    var caseOfficerPerson = PersonTestUtil.createDefaultPerson();
    var caseOfficerAssignment = new Assignment
        (pwaApplication.getBusinessKey(), WorkflowType.PWA_APPLICATION, WorkflowAssignment.CASE_OFFICER, caseOfficerPerson.getId());
    when(assignmentService.getAssignmentOrError(pwaApplication, WorkflowAssignment.CASE_OFFICER)).thenReturn(caseOfficerAssignment);
    when(personService.getPersonById(caseOfficerAssignment.getAssigneePersonId())).thenReturn(caseOfficerPerson);

    var form = PublicNoticeApprovalTestUtil.createRejectedPublicNoticeForm();
    publicNoticeApprovalService.updatePublicNoticeRequest(form, pwaApplication, user);


    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.DRAFT);

    verify(publicNoticeService, times(1)).savePublicNoticeRequest(publicNoticeRequestArgumentCaptor.capture());
    var actualPublicNoticeRequest = publicNoticeRequestArgumentCaptor.getValue();
    assertThat(actualPublicNoticeRequest.getRequestApproved()).isFalse();
    assertThat(actualPublicNoticeRequest.getResponderPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());
    assertThat(actualPublicNoticeRequest.getResponseTimestamp()).isEqualTo(clock.instant());
    assertThat(actualPublicNoticeRequest.getRejectionReason()).isEqualTo(form.getRequestRejectedReason());
    assertThat(actualPublicNoticeRequest.getStatus()).isEqualTo(PublicNoticeRequestStatus.REJECTED);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(publicNotice, form.getRequestApproved());
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.MANAGER_APPROVAL));

    List.of(caseOfficerPerson).forEach(teamMember -> {
      var expectedEmailProps = new PublicNoticeRejectedEmailProps(
          caseOfficerPerson.getFullName(),
          pwaApplication.getAppReference(),
          form.getRequestRejectedReason(),
          caseManagementLink);

      verify(emailService, times(1)).sendEmail(expectedEmailProps, caseOfficerPerson, pwaApplication.getAppReference());
    });

  }


  @Test
  void validate_verifyServiceInteractions() {

    var form = new PublicNoticeApprovalForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeApprovalService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);

  }


}
