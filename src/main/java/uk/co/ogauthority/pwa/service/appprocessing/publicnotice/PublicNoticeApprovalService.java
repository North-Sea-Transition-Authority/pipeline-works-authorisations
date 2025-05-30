package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.AssignmentService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeApprovedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticeRejectedEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeApprovalResult;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeApprovalValidator;

@Service
public class PublicNoticeApprovalService {


  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeApprovalValidator publicNoticeApprovalValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;
  private final CaseLinkService caseLinkService;
  private final PwaContactService pwaContactService;
  private final PersonService personService;
  private final AssignmentService assignmentService;
  private final EmailService emailService;

  @Autowired
  public PublicNoticeApprovalService(
      PublicNoticeService publicNoticeService,
      PublicNoticeApprovalValidator publicNoticeApprovalValidator,
      CamundaWorkflowService camundaWorkflowService,
      @Qualifier("utcClock") Clock clock,
      CaseLinkService caseLinkService,
      PwaContactService pwaContactService,
      PersonService personService,
      AssignmentService assignmentService,
      EmailService emailService) {
    this.publicNoticeService = publicNoticeService;
    this.publicNoticeApprovalValidator = publicNoticeApprovalValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
    this.caseLinkService = caseLinkService;
    this.pwaContactService = pwaContactService;
    this.personService = personService;
    this.assignmentService = assignmentService;
    this.emailService = emailService;
  }


  public boolean openPublicNoticeCanBeApproved(PwaApplication pwaApplication) {
    return publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }


  @Transactional
  public void updatePublicNoticeRequest(PublicNoticeApprovalForm form,
                                        PwaApplication pwaApplication,
                                        AuthenticatedUserAccount authenticatedUserAccount) {

    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var publicNoticeRequest = publicNoticeService.getLatestPublicNoticeRequest(publicNotice);
    var requestIsApproved = PwaApplicationPublicNoticeApprovalResult.REQUEST_APPROVED.equals(form.getRequestApproved());

    publicNoticeRequest.setRequestApproved(requestIsApproved);
    publicNoticeRequest.setResponderPersonId(authenticatedUserAccount.getLinkedPerson().getId().asInt());
    publicNoticeRequest.setResponseTimestamp(clock.instant());

    if (!requestIsApproved) {
      publicNoticeRequest.setStatus(PublicNoticeRequestStatus.REJECTED);
      publicNoticeRequest.setRejectionReason(form.getRequestRejectedReason());
      publicNotice.setStatus(PublicNoticeStatus.DRAFT);

    } else {
      publicNoticeRequest.setStatus(PublicNoticeRequestStatus.APPROVED);
      publicNotice.setStatus(PublicNoticeStatus.APPLICANT_UPDATE);
    }

    publicNoticeService.savePublicNoticeRequest(publicNoticeRequest);
    publicNoticeService.savePublicNotice(publicNotice);
    camundaWorkflowService.setWorkflowProperty(publicNotice, form.getRequestApproved());
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.MANAGER_APPROVAL));

    getRecipientsForApprovalEmail(pwaApplication, requestIsApproved)
        .forEach(recipient -> {
          var emailProps = buildApprovalEmailProps(
              pwaApplication, requestIsApproved, recipient.getFullName(), form.getRequestRejectedReason());
          emailService.sendEmail(emailProps, recipient, pwaApplication.getAppReference());
        });
  }


  private List<Person> getRecipientsForApprovalEmail(PwaApplication pwaApplication, boolean requestApproved) {

    if (requestApproved) {
      return pwaContactService.getPeopleInRoleForPwaApplication(
          pwaApplication,
          PwaContactRole.PREPARER
      );

    } else {
      var caseOfficerAssignment = assignmentService.getAssignmentOrError(pwaApplication, WorkflowAssignment.CASE_OFFICER);
      return List.of(personService.getPersonById(caseOfficerAssignment.getAssigneePersonId()));
    }

  }

  private EmailProperties buildApprovalEmailProps(PwaApplication pwaApplication, boolean requestApproved,
                                                  String recipientName, String rejectionReason) {

    var caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplication);

    if (requestApproved) {
      return new PublicNoticeApprovedEmailProps(
          recipientName,
          pwaApplication.getAppReference(),
          caseManagementLink);

    } else {
      return new PublicNoticeRejectedEmailProps(
          recipientName,
          pwaApplication.getAppReference(),
          rejectionReason,
          caseManagementLink);
    }
  }


  public BindingResult validate(PublicNoticeApprovalForm form, BindingResult bindingResult) {
    publicNoticeApprovalValidator.validate(form, bindingResult);
    return bindingResult;
  }






}
