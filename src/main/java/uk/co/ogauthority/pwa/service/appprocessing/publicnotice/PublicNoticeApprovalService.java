package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import java.time.Clock;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeApprovedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticeRejectedEmailProps;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeApprovalResult;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeApprovalValidator;

@Service
public class PublicNoticeApprovalService {


  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeApprovalValidator publicNoticeApprovalValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;
  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final PwaContactService pwaContactService;
  private final PersonService personService;
  private final AssignmentService assignmentService;

  @Autowired
  public PublicNoticeApprovalService(
      PublicNoticeService publicNoticeService,
      PublicNoticeApprovalValidator publicNoticeApprovalValidator,
      CamundaWorkflowService camundaWorkflowService,
      @Qualifier("utcClock") Clock clock,
      NotifyService notifyService,
      EmailCaseLinkService emailCaseLinkService,
      PwaContactService pwaContactService,
      PersonService personService, AssignmentService assignmentService) {
    this.publicNoticeService = publicNoticeService;
    this.publicNoticeApprovalValidator = publicNoticeApprovalValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.pwaContactService = pwaContactService;
    this.personService = personService;
    this.assignmentService = assignmentService;
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
      publicNoticeRequest.setRejectionReason(form.getRequestRejectedReason());
      publicNotice.setStatus(PublicNoticeStatus.DRAFT);

    } else {
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
          notifyService.sendEmail(emailProps, recipient.getEmailAddress());
        });
  }


  private List<Person> getRecipientsForApprovalEmail(PwaApplication pwaApplication, boolean requestApproved) {

    if (requestApproved) {
      return pwaContactService.getPeopleInRoleForPwaApplication(
          pwaApplication,
          PwaContactRole.PREPARER
      );

    } else {
      var caseOfficerAssignment = assignmentService.getCaseOfficerAssignment(pwaApplication);
      return List.of(personService.getPersonById(caseOfficerAssignment.getAssigneePersonId()));
    }

  }

  private EmailProperties buildApprovalEmailProps(PwaApplication pwaApplication, boolean requestApproved,
                                                  String recipientName, String rejectionReason) {

    var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);

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
