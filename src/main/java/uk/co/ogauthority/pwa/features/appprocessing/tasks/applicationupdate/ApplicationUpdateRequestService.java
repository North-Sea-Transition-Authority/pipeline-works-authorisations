package uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.updaterequests.ApplicationUpdateRequestEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.updaterequests.ApplicationUpdateResponseEmailProps;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.integrations.camunda.external.GenericMessageEvent;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.OptionsApprovalStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class ApplicationUpdateRequestService implements AppProcessingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUpdateRequestService.class);

  private final ApplicationUpdateRequestRepository applicationUpdateRequestRepository;
  private final Clock clock;
  private final PwaContactService pwaContactService;
  private final PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final PersonService personService;
  private final ApproveOptionsService approveOptionsService;
  private final CaseLinkService caseLinkService;
  private final EmailService emailService;

  @Autowired
  public ApplicationUpdateRequestService(ApplicationUpdateRequestRepository applicationUpdateRequestRepository,
                                         @Qualifier("utcClock") Clock clock,
                                         PwaContactService pwaContactService,
                                         PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService,
                                         WorkflowAssignmentService workflowAssignmentService,
                                         PersonService personService,
                                         ApproveOptionsService approveOptionsService,
                                         CaseLinkService caseLinkService,
                                         EmailService emailService) {
    this.applicationUpdateRequestRepository = applicationUpdateRequestRepository;
    this.clock = clock;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailVersioningService = pwaApplicationDetailVersioningService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.personService = personService;
    this.approveOptionsService = approveOptionsService;
    this.caseLinkService = caseLinkService;
    this.emailService = emailService;
  }


  @Transactional
  public void submitApplicationUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                             WebUserAccount requestingUser,
                                             ApplicationUpdateRequestForm form) {

    // The update request was made for a specific version, so the original is linked.
    var deadlineTimestamp = DateUtils.datePickerStringToDate(form.getDeadlineTimestampStr())
        .atStartOfDay(ZoneId.systemDefault()).toInstant();
    createApplicationUpdateRequest(pwaApplicationDetail, requestingUser.getLinkedPerson(), form.getRequestReason(), deadlineTimestamp);

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

  private Optional<ApplicationUpdateRequest> getOpenUpdateRequest(PwaApplicationDetail pwaApplicationDetail) {
    return applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN);
  }

  private ApplicationUpdateRequest getOpenUpdateRequestOrThrow(PwaApplicationDetail pwaApplicationDetail) {
    return getOpenUpdateRequest(pwaApplicationDetail).orElseThrow(() -> new PwaEntityNotFoundException(
            "Expected to find open update request for pad_id:" + pwaApplicationDetail.getId()));
  }

  public List<ApplicationUpdateRequest> getApplicationUpdateRequests(List<PwaApplicationDetail> applicationDetails) {
    return applicationUpdateRequestRepository.findAllByPwaApplicationDetailIn(applicationDetails);
  }

  @VisibleForTesting
  void createApplicationUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                      Person requestingPerson,
                                      String requestReason,
                                      Instant deadlineTimestamp) {
    var updateRequest = ApplicationUpdateRequest.createRequest(
        pwaApplicationDetail,
        requestingPerson,
        clock,
        requestReason,
        deadlineTimestamp);

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
          emailService.sendEmail(
              new ApplicationUpdateRequestEmailProps(
                  person.getFullName(),
                  pwaApplicationDetail.getPwaApplicationRef(),
                  requestingPerson.getFullName(),
                  caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())),
              person,
              pwaApplicationDetail.getPwaApplicationRef()
          )
      );

    } else {
      LOGGER.error("Tried to send application update request email, but no recipients found. pwaApplication.id: {}",
          pwaApplicationDetail.getMasterPwaApplicationId());
    }

  }

  private void sendApplicationUpdateRespondedEmail(PwaApplicationDetail pwaApplicationDetail, Person requestedByperson) {
    emailService.sendEmail(
        new ApplicationUpdateResponseEmailProps(
            requestedByperson.getFullName(),
            pwaApplicationDetail.getPwaApplicationRef(),
            caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())),
        requestedByperson,
        pwaApplicationDetail.getPwaApplicationRef()
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
    return !ApplicationState.ENDED.includes(processingContext.getApplicationDetailStatus())
        && processingContext.getAppProcessingPermissions().contains(
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

  @Transactional
  public void endUpdateRequestIfExists(PwaApplicationDetail pwaApplicationDetail) {

    getOpenUpdateRequest(pwaApplicationDetail).ifPresent((openUpdateRequest) -> {
      openUpdateRequest.setStatus(ApplicationUpdateRequestStatus.ENDED);
      applicationUpdateRequestRepository.save(openUpdateRequest);
    });
  }
}
