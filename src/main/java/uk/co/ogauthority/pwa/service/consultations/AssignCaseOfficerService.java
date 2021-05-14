package uk.co.ogauthority.pwa.service.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.AssignCaseOfficerForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.assignments.ApplicationAssignedToYouEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.assignments.CaseOfficerAssignedEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.validators.consultations.AssignCaseOfficerValidator;

@Service
public class AssignCaseOfficerService implements AppProcessingService {

  private final WorkflowAssignmentService workflowAssignmentService;
  private final TeamManagementService teamManagementService;
  private final NotifyService notifyService;
  private final PersonService personService;
  private final AssignCaseOfficerValidator assignCaseOfficerValidator;
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  public AssignCaseOfficerService(
      WorkflowAssignmentService workflowAssignmentService,
      TeamManagementService teamManagementService,
      NotifyService notifyService,
      PersonService personService,
      AssignCaseOfficerValidator assignCaseOfficerValidator,
      EmailCaseLinkService emailCaseLinkService) {
    this.workflowAssignmentService = workflowAssignmentService;
    this.teamManagementService = teamManagementService;
    this.notifyService = notifyService;
    this.personService = personService;
    this.assignCaseOfficerValidator = assignCaseOfficerValidator;
    this.emailCaseLinkService = emailCaseLinkService;
  }

  public void assignCaseOfficer(PersonId caseOfficerPersonId,
                                PwaApplicationDetail pwaApplicationDetail,
                                AuthenticatedUserAccount assigningUser) {

    var caseOfficer = teamManagementService.getPerson(caseOfficerPersonId.asInt());

    workflowAssignmentService.assign(
        pwaApplicationDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficer,
        assigningUser.getLinkedPerson());

    sendCaseOfficerAssignedEmail(pwaApplicationDetail, caseOfficer);
    sendCaseOfficerAssignedPersonalEmail(pwaApplicationDetail, caseOfficer, assigningUser.getLinkedPerson().getFullName());
  }

  private void sendCaseOfficerAssignedEmail(PwaApplicationDetail applicationDetail, Person caseOfficer) {
    var submitterPerson = personService.getPersonById(applicationDetail.getSubmittedByPersonId());

    var props = new CaseOfficerAssignedEmailProps(
        submitterPerson.getFullName(), applicationDetail.getPwaApplicationRef(), caseOfficer.getFullName());
    notifyService.sendEmail(props, submitterPerson.getEmailAddress());
  }

  private void sendCaseOfficerAssignedPersonalEmail(PwaApplicationDetail applicationDetail,
                                                    Person caseOfficer,
                                                    String assigningUserFullName) {
    var props = new ApplicationAssignedToYouEmailProps(caseOfficer.getFullName(), applicationDetail.getPwaApplicationRef(),
        assigningUserFullName,
        emailCaseLinkService.generateCaseManagementLink(applicationDetail.getPwaApplication()));
    notifyService.sendEmail(props, caseOfficer.getEmailAddress());
  }

  public BindingResult validate(AssignCaseOfficerForm form, BindingResult bindingResult,
                                PwaApplication pwaApplication) {
    assignCaseOfficerValidator.validate(form, bindingResult, pwaApplication);
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER)
        && !processingContext.getApplicationDetail().getStatus().equals(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
  }

}
