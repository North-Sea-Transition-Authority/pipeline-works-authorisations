package uk.co.ogauthority.pwa.service.appprocessing.applicationupdate;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.applicationupdates.ApplicationUpdateRequestStatus;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.notify.emailproperties.updaterequests.ApplicationUpdateRequestEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.updaterequests.ApplicationUpdateResponseEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.workflow.GenericMessageEvent;
import uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates.ApplicationUpdateRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.OptionsApprovalStatus;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;

@Service
public class ApplicationUpdateRequestService implements AppProcessingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUpdateRequestService.class);

  private final ApplicationUpdateRequestRepository applicationUpdateRequestRepository;
  private final Clock clock;
  private final NotifyService notifyService;
  private final PwaContactService pwaContactService;
  private final PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final PersonService personService;
  private final ApproveOptionsService approveOptionsService;
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  public ApplicationUpdateRequestService(ApplicationUpdateRequestRepository applicationUpdateRequestRepository,
                                         @Qualifier("utcClock") Clock clock,
                                         NotifyService notifyService,
                                         PwaContactService pwaContactService,
                                         PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService,
                                         WorkflowAssignmentService workflowAssignmentService,
                                         PersonService personService,
                                         ApproveOptionsService approveOptionsService,
                                         EmailCaseLinkService emailCaseLinkService) {
    this.applicationUpdateRequestRepository = applicationUpdateRequestRepository;
    this.clock = clock;
    this.notifyService = notifyService;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailVersioningService = pwaApplicationDetailVersioningService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.personService = personService;
    this.approveOptionsService = approveOptionsService;
    this.emailCaseLinkService = emailCaseLinkService;
  }


  @Transactional
  public void submitApplicationUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                             WebUserAccount requestingUser,
                                             String requestReason) {

    // The update request was made for a specific version, so the original is linked.
    createApplicationUpdateRequest(pwaApplicationDetail, requestingUser.getLinkedPerson(), requestReason);

    // then a new detail is created which is what will be resubmitted with any changes.
    var newTipDetail = pwaApplicationDetailVersioningService
        .createNewApplicationVersion(pwaApplicationDetail, requestingUser);

    // then we attempt to send an email to alert the application preparers that changes are required.
    sendApplicationUpdateRequestedEmail(newTipDetail, requestingUser.getLinkedPerson());

    // update workflow
    workflowAssignmentService.triggerWorkflowMessageAndAssertTaskExists(
        GenericMessageEvent.from(
            newTipDetail.getPwaApplication(),
            PwaApplicationWorkflowMessageEvents.UPDATE_APPLICATION_REQUEST.getMessageEventName()),
        PwaApplicationWorkflowTask.UPDATE_APPLICATION);

  }

  /**
   * Submit a response to an open update request to the OGA.
   * @param pwaApplicationDetail the update request is for
   * @param respondingPerson person submitting response
   * @param response response text
   */
  @Transactional
  public void respondToApplicationOpenUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                                    Person respondingPerson,
                                                    String response) {

    var openUpdateRequest = getOpenUpdateRequestOrThrow(pwaApplicationDetail);

    // only update the response if it isn't already present to avoid overwriting a preparer's response
    if (openUpdateRequest.getResponseByPersonId() == null) {
      openUpdateRequest.setResponseOtherChanges(response);
      openUpdateRequest.setResponseByPersonId(respondingPerson.getId());
    }

    openUpdateRequest.setResponseTimestamp(clock.instant());
    openUpdateRequest.setResponsePwaApplicationDetail(pwaApplicationDetail);
    openUpdateRequest.setStatus(ApplicationUpdateRequestStatus.RESPONDED);
    applicationUpdateRequestRepository.save(openUpdateRequest);

    var requestedByPerson = personService.getPersonById(openUpdateRequest.getRequestedByPersonId());
    sendApplicationUpdateRespondedEmail(pwaApplicationDetail, requestedByPerson);

  }

  /**
   * Store a user's response to an open update request without submitting it to OGA. Allows for users other than those
   * preparing the application to submit the app while the response is provided by the preparer.
   * @param pwaApplicationDetail the update request is for
   * @param respondingPerson person responding
   * @param responseText response text
   */
  @Transactional
  public void storeResponseWithoutSubmitting(PwaApplicationDetail pwaApplicationDetail,
                                             Person respondingPerson,
                                             String responseText) {

    var openUpdateRequest = getOpenUpdateRequestOrThrow(pwaApplicationDetail);
    openUpdateRequest.setResponseOtherChanges(responseText);
    openUpdateRequest.setResponseByPersonId(respondingPerson.getId());
    openUpdateRequest.setResponsePwaApplicationDetail(pwaApplicationDetail);
    openUpdateRequest.setResponseTimestamp(clock.instant());
    applicationUpdateRequestRepository.save(openUpdateRequest);

  }

  private ApplicationUpdateRequest getOpenUpdateRequestOrThrow(PwaApplicationDetail pwaApplicationDetail) {
    return applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Expected to find open update request for pad_id:" + pwaApplicationDetail.getId()));
  }

  @VisibleForTesting
  void createApplicationUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                      Person requestingPerson,
                                      String requestReason) {
    var updateRequest = ApplicationUpdateRequest.createRequest(
        pwaApplicationDetail,
        requestingPerson,
        clock,
        requestReason);

    applicationUpdateRequestRepository.save(updateRequest);

  }

  @VisibleForTesting
  void sendApplicationUpdateRequestedEmail(PwaApplicationDetail pwaApplicationDetail, Person requestingPerson) {

    var recipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER
    );

    if (!recipients.isEmpty()) {
      recipients.forEach(person ->
          notifyService.sendEmail(
              new ApplicationUpdateRequestEmailProps(
                  person.getFullName(),
                  pwaApplicationDetail.getPwaApplicationRef(),
                  requestingPerson.getFullName(),
                  emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())),
              person.getEmailAddress()
          )
      );

    } else {
      LOGGER.error("Tried to send application update request email, but no recipients found. pwaApplication.id: {}",
          pwaApplicationDetail.getMasterPwaApplicationId());
    }

  }

  private void sendApplicationUpdateRespondedEmail(PwaApplicationDetail pwaApplicationDetail, Person requestedByperson) {

    notifyService.sendEmail(
        new ApplicationUpdateResponseEmailProps(
            requestedByperson.getFullName(),
            pwaApplicationDetail.getPwaApplicationRef(),
            emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())),
        requestedByperson.getEmailAddress()
    );

  }

  public boolean applicationHasOpenUpdateRequest(PwaApplicationDetail pwaApplicationDetail) {
    return applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(),
        ApplicationUpdateRequestStatus.OPEN
    ).isPresent();
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(
        PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {
    var optionsApprovalStatus = approveOptionsService.getOptionsApprovalStatus(processingContext.getApplicationDetail());

    boolean openUpdateForDetail = applicationHasOpenUpdateRequest(processingContext.getApplicationDetail());

    // prevent access during initial options approval or in progress update request
    var taskState = openUpdateForDetail || OptionsApprovalStatus.APPROVED_UNRESPONDED.equals(optionsApprovalStatus)
        ? TaskState.LOCK
        : TaskState.EDIT;

    var taskTag = openUpdateForDetail ? TaskTag.from(TaskStatus.IN_PROGRESS) : null;

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        taskTag,
        taskState,
        task.getDisplayOrder());

  }
}
