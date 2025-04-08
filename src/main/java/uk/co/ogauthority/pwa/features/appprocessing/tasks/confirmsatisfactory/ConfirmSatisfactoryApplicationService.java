package uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.updaterequests.ApplicationUpdateAcceptedEmailProps;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class ConfirmSatisfactoryApplicationService implements AppProcessingService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ConsultationRequestService consultationRequestService;
  private final CaseLinkService caseLinkService;

  private final PadPipelineTransferService pipelineTransferService;
  private final EmailService emailService;

  @Autowired
  public ConfirmSatisfactoryApplicationService(PwaApplicationDetailService pwaApplicationDetailService,
                                               ConsultationRequestService consultationRequestService,
                                               CaseLinkService caseLinkService,
                                               PadPipelineTransferService pipelineTransferService,
                                               EmailService emailService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.consultationRequestService = consultationRequestService;
    this.caseLinkService = caseLinkService;
    this.pipelineTransferService = pipelineTransferService;
    this.emailService = emailService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {

    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CONFIRM_SATISFACTORY_APPLICATION)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY)

        //completed apps do not have an assigned c.o on the app involvement therefore we're showing the task
        // for any case officer viewing a completed app as the task will never be accessible for a completed app
        || (processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA)
        && processingContext.getApplicationDetail().getStatus().equals(PwaApplicationStatus.COMPLETE));
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    boolean isSatisfactory = isSatisfactory(processingContext.getApplicationDetail());

    var taskState = TaskState.LOCK;

    if (processingContext.getApplicationDetail().getStatus().equals(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        && !processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY)) {
      taskState = !isSatisfactory ? TaskState.EDIT : TaskState.LOCK;
    }
    if (getTaskStatus(processingContext) == TaskStatus.AWAITING_CLAIM) {
      taskState = TaskState.LOCK;
    }

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        TaskTag.from(getTaskStatus(processingContext)),
        taskState,
        task.getDisplayOrder());

  }

  /**
   * Task is accessible if the latest version of the application hasn't been confirmed satisfactory.
   * If the latest version also contains a pipeline transfer, that transfer must be claimed.
   */
  public boolean taskAccessible(PwaAppProcessingContext context) {
    var pipelineTransfer = pipelineTransferService.findUnclaimedByDonorApplication(context.getApplicationDetail());
    if (pipelineTransfer.isEmpty()) {
      return !isSatisfactory(context.getApplicationDetail());
    }
    return false;
  }

  public boolean isSatisfactory(PwaApplicationDetail applicationDetail) {
    return applicationDetail.getConfirmedSatisfactoryTimestamp() != null;
  }

  public boolean atLeastOneSatisfactoryVersion(PwaApplication pwaApplication) {
    return pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(pwaApplication).stream()
        .anyMatch(this::isSatisfactory);
  }

  public boolean confirmSatisfactoryTaskRequired(PwaApplicationDetail tipDetail) {
    return !tipDetail.isFirstVersion() && !isSatisfactory(tipDetail);
  }

  public TaskStatus getTaskStatus(PwaAppProcessingContext context) {
    var pipelineTransfer = pipelineTransferService.findUnclaimedByDonorApplication(context.getApplicationDetail());
    if (!pipelineTransfer.isEmpty()) {
      return TaskStatus.AWAITING_CLAIM;
    } else if (isSatisfactory(context.getApplicationDetail())) {
      return TaskStatus.COMPLETED;
    }
    return TaskStatus.NOT_STARTED;
  }

  @Transactional
  public void confirmSatisfactory(PwaApplicationDetail applicationDetail,
                                  String reason,
                                  Person confirmingPerson) {

    if (isSatisfactory(applicationDetail)) {
      throw new IllegalStateException(String.format(
          "Cannot confirm app detail satisfactory as it is already satisfactory. pad_id [%s]", applicationDetail.getId()));
    }

    pwaApplicationDetailService.setConfirmedSatisfactoryData(applicationDetail, reason, confirmingPerson);

    var openConsultationRequests = consultationRequestService.getAllOpenRequestsByApplication(applicationDetail.getPwaApplication());
    var consulteeGroupAndDetailMap = consultationRequestService.getGroupDetailsForConsulteeGroups(openConsultationRequests);

    openConsultationRequests.forEach(consultationRequest -> {
      var assignedResponder = consultationRequestService.getAssignedResponderForConsultation(consultationRequest);
      List<Person> recipients = assignedResponder == null
          ? consultationRequestService.getConsultationRecipients(consultationRequest) :  List.of(assignedResponder);

      recipients.forEach(recipient -> sendEmail(recipient,
          consultationRequest,
          consulteeGroupAndDetailMap.get(consultationRequest.getConsulteeGroup()).getName(),
          caseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication())));
    });


  }

  private void sendEmail(Person recipient,
                         ConsultationRequest consultationRequest,
                         String consulteeGroupName,
                         String caseManagementLink) {
    var emailProps = new ApplicationUpdateAcceptedEmailProps(
        recipient.getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupName,
        caseManagementLink);

    emailService.sendEmail(emailProps, recipient, consultationRequest.getPwaApplication().getAppReference());
  }
}
