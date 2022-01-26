package uk.co.ogauthority.pwa.service.consultations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.assignments.ApplicationAssignedToYouEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.assignments.CaseOfficerAssignedEmailProps;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.AssignCaseOfficerForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.validators.consultations.AssignCaseOfficerValidator;

@Service
public class AssignCaseOfficerService implements AppProcessingService {

  private final WorkflowAssignmentService workflowAssignmentService;
  private final TeamManagementService teamManagementService;
  private final NotifyService notifyService;
  private final PersonService personService;
  private final AssignCaseOfficerValidator assignCaseOfficerValidator;
  private final CaseLinkService caseLinkService;

  @Autowired
  public AssignCaseOfficerService(
      WorkflowAssignmentService workflowAssignmentService,
      TeamManagementService teamManagementService,
      NotifyService notifyService,
      PersonService personService,
      AssignCaseOfficerValidator assignCaseOfficerValidator,
      CaseLinkService caseLinkService) {
    this.workflowAssignmentService = workflowAssignmentService;
    this.teamManagementService = teamManagementService;
    this.notifyService = notifyService;
    this.personService = personService;
    this.assignCaseOfficerValidator = assignCaseOfficerValidator;
    this.caseLinkService = caseLinkService;
  }

  /**
   * Used when a user wants to set a specific user as the case officer for an application.
   * @param pwaApplicationDetail being affected
   * @param caseOfficerPersonId person id of the new case officer
   * @param assigningUser user doing the assignment
   */
  public void assignCaseOfficer(PwaApplicationDetail pwaApplicationDetail,
                                PersonId caseOfficerPersonId,
                                AuthenticatedUserAccount assigningUser) {

    var caseOfficer = teamManagementService.getPerson(caseOfficerPersonId.asInt());

    workflowAssignmentService.assign(
        pwaApplicationDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficer,
        assigningUser.getLinkedPerson());

    sendCaseOfficerAssignmentEmails(pwaApplicationDetail, caseOfficer, assigningUser.getLinkedPerson());

  }

  /**
   * Used to assign a case officer ignoring any exception that might occur as a result. Only to be used for system
   * assignments, not for those directly triggered by a user.
   * @param pwaApplicationDetail being affected
   * @param caseOfficerPerson person to assign as case officer
   * @param assigningPerson person doing the assigning (someone must have chosen, the system does not choose)
   * @return the resulting status of the assignment
   */
  public WorkflowAssignmentService.AssignTaskResult autoAssignCaseOfficer(PwaApplicationDetail pwaApplicationDetail,
                                                                          Person caseOfficerPerson,
                                                                          Person assigningPerson) {

    var assignmentResult = workflowAssignmentService.assignTaskNoException(
        pwaApplicationDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson,
        assigningPerson
    );

    if (assignmentResult.equals(WorkflowAssignmentService.AssignTaskResult.SUCCESS)) {
      sendCaseOfficerAssignmentEmails(pwaApplicationDetail, caseOfficerPerson, assigningPerson);
    }

    return assignmentResult;

  }

  private void sendCaseOfficerAssignmentEmails(PwaApplicationDetail pwaApplicationDetail,
                                               Person caseOfficer,
                                               Person assigningPerson) {

    sendCaseOfficerAssignedEmailToApplicant(pwaApplicationDetail, caseOfficer);
    sendCaseOfficerAssignedEmailToImplicatedCo(pwaApplicationDetail, caseOfficer, assigningPerson);

  }

  private void sendCaseOfficerAssignedEmailToApplicant(PwaApplicationDetail applicationDetail, Person caseOfficer) {

    var submitterPerson = personService.getPersonById(applicationDetail.getSubmittedByPersonId());

    var props = new CaseOfficerAssignedEmailProps(
        submitterPerson.getFullName(),
        applicationDetail.getPwaApplicationRef(),
        caseOfficer.getFullName(),
        caseLinkService.generateCaseManagementLink(applicationDetail.getPwaApplication()));
    notifyService.sendEmail(props, submitterPerson.getEmailAddress());

  }

  private void sendCaseOfficerAssignedEmailToImplicatedCo(PwaApplicationDetail applicationDetail,
                                                          Person caseOfficer,
                                                          Person assigningPerson) {

    var props = new ApplicationAssignedToYouEmailProps(
        caseOfficer.getFullName(),
        applicationDetail.getPwaApplicationRef(),
        assigningPerson.getFullName(),
        caseLinkService.generateCaseManagementLink(applicationDetail.getPwaApplication()));
    notifyService.sendEmail(props, caseOfficer.getEmailAddress());

  }

  public BindingResult validate(AssignCaseOfficerForm form, BindingResult bindingResult,
                                PwaApplication pwaApplication) {
    assignCaseOfficerValidator.validate(form, bindingResult, pwaApplication);
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    //We only want to show the task when in 'Case Officer Review' as before this there is no required case officer
    // and at consent review the user can choose which case officer to send it back to.
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER)
        && processingContext.getApplicationDetail().getStatus().equals(PwaApplicationStatus.CASE_OFFICER_REVIEW);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        null,
        canShowInTaskList(processingContext) ? TaskState.EDIT : TaskState.LOCK,
        task.getDisplayOrder());

  }

}