package uk.co.ogauthority.pwa.service.pwaconsents;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ConsentIssuedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ConsentReviewReturnedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.HolderSubmitterConsentIssuedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.NonHolderConsentIssuedEmailProps;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;

@Service
public class ConsentEmailService {

  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final PersonService personService;
  private final AssignmentService assignmentService;

  @Autowired
  public ConsentEmailService(NotifyService notifyService,
                             EmailCaseLinkService emailCaseLinkService,
                             PersonService personService,
                             AssignmentService assignmentService) {
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.personService = personService;
    this.assignmentService = assignmentService;
  }

  public void sendConsentReviewReturnedEmail(PwaApplicationDetail pwaApplicationDetail,
                                             String recipientEmail,
                                             String recipientFullName,
                                             String returningPersonName,
                                             String returnReason) {
    var emailProps = new ConsentReviewReturnedEmailProps(
        recipientFullName,
        pwaApplicationDetail.getPwaApplicationRef(),
        returningPersonName,
        returnReason,
        emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication()));

    notifyService.sendEmail(emailProps, recipientEmail);

  }

  public void sendConsentIssuedEmail(PwaApplicationDetail pwaApplicationDetail,
                                     String issuingPersonName) {
    var caseOfficerPerson = getAssignedCaseOfficerPerson(pwaApplicationDetail);
    var emailProps = new ConsentIssuedEmailProps(
        caseOfficerPerson.getFullName(),
        pwaApplicationDetail.getPwaApplicationRef(),
        issuingPersonName
    );

    notifyService.sendEmail(emailProps, caseOfficerPerson.getEmailAddress());

  }

  public void sendHolderAndSubmitterConsentIssuedEmail(PwaApplicationDetail pwaApplicationDetail,
                                                       String coverLetter,
                                                       Collection<Person> emailRecipientPersons) {

    var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());
    emailRecipientPersons.forEach(emailRecipientPerson -> {
      var emailProps = new HolderSubmitterConsentIssuedEmailProps(
          emailRecipientPerson.getFullName(),
          pwaApplicationDetail.getPwaApplicationRef(),
          coverLetter,
          caseManagementLink
      );
      notifyService.sendEmail(emailProps, emailRecipientPerson.getEmailAddress());
    });
  }

  public void sendNonHolderConsentIssuedEmail(PwaApplicationDetail pwaApplicationDetail,
                                              String coverLetter,
                                              List<Person> emailRecipientPersons) {
    emailRecipientPersons.forEach(emailRecipientPerson -> {
      var emailProps = new NonHolderConsentIssuedEmailProps(
          emailRecipientPerson.getFullName(),
          pwaApplicationDetail.getPwaApplicationRef(),
          coverLetter
      );
      notifyService.sendEmail(emailProps, emailRecipientPerson.getEmailAddress());
    });
  }

  private Person getAssignedCaseOfficerPerson(PwaApplicationDetail pwaApplicationDetail) {
    var caseOfficerPersonId = assignmentService.getAssignmentsForWorkflowAssignment(pwaApplicationDetail.getPwaApplication(),
        WorkflowAssignment.CASE_OFFICER).orElseThrow(
            () -> new IllegalStateException(String.format("Cannot find assignment for business key %s and workflow assignment %s",
                pwaApplicationDetail.getPwaApplication().getId(), WorkflowAssignment.CASE_OFFICER)))
        .getAssigneePersonId();
    return personService.getPersonById(caseOfficerPersonId);
  }

}
