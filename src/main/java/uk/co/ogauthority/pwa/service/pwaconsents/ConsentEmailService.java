package uk.co.ogauthority.pwa.service.pwaconsents;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.AssignmentService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.CaseOfficerConsentIssuedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ConsentIssuedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ConsentReviewReturnedEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class ConsentEmailService {

  private final CaseLinkService caseLinkService;
  private final PersonService personService;
  private final AssignmentService assignmentService;
  private final EmailService emailService;

  @Autowired
  public ConsentEmailService(CaseLinkService caseLinkService,
                             PersonService personService,
                             AssignmentService assignmentService,
                             EmailService emailService) {
    this.caseLinkService = caseLinkService;
    this.personService = personService;
    this.assignmentService = assignmentService;
    this.emailService = emailService;
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
        caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication()));

    emailService.sendEmail(
        emailProps,
        EmailRecipient.directEmailAddress(recipientEmail),
        pwaApplicationDetail.getPwaApplicationRef()
    );
  }

  public void sendCaseOfficerConsentIssuedEmail(PwaApplicationDetail pwaApplicationDetail,
                                                String issuingPersonName) {
    var caseOfficerPerson = getAssignedCaseOfficerPerson(pwaApplicationDetail);
    var emailProps = new CaseOfficerConsentIssuedEmailProps(
        caseOfficerPerson.getFullName(),
        pwaApplicationDetail.getPwaApplicationRef(),
        issuingPersonName
    );

    emailService.sendEmail(emailProps, caseOfficerPerson, pwaApplicationDetail.getPwaApplicationRef());
  }

  public void sendHolderAndSubmitterConsentIssuedEmail(PwaApplicationDetail pwaApplicationDetail,
                                                       String consentReference,
                                                       String coverLetter,
                                                       String caseOfficerEmail,
                                                       Collection<Person> emailRecipientPersons) {

    var caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());

    emailRecipientPersons.forEach(emailRecipientPerson -> {

      var emailProps = new ConsentIssuedEmailProps(
          pwaApplicationDetail.getPwaApplicationType().getConsentIssueEmail().getHolderEmailTemplate(),
          emailRecipientPerson.getFullName(),
          pwaApplicationDetail.getPwaApplicationRef(),
          consentReference,
          coverLetter,
          caseOfficerEmail,
          caseManagementLink
      );

      emailService.sendEmail(emailProps, emailRecipientPerson, pwaApplicationDetail.getPwaApplicationRef());
    });

  }

  public void sendNonHolderConsentIssuedEmail(PwaApplicationDetail pwaApplicationDetail,
                                              String consentReference,
                                              String coverLetter,
                                              String caseOfficerEmail,
                                              List<Person> emailRecipientPersons) {

    var caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());

    emailRecipientPersons.forEach(emailRecipientPerson -> {

      var emailProps = new ConsentIssuedEmailProps(
          pwaApplicationDetail.getPwaApplicationType().getConsentIssueEmail().getNonHolderEmailTemplate(),
          emailRecipientPerson.getFullName(),
          pwaApplicationDetail.getPwaApplicationRef(),
          consentReference,
          coverLetter,
          caseOfficerEmail,
          caseManagementLink
      );

      emailService.sendEmail(emailProps, emailRecipientPerson, pwaApplicationDetail.getPwaApplicationRef());

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
