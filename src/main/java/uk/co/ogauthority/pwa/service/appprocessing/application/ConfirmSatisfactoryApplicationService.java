package uk.co.ogauthority.pwa.service.appprocessing.application;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.updaterequests.ApplicationUpdateAcceptedEmailProps;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class ConfirmSatisfactoryApplicationService implements AppProcessingService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ConsultationRequestService consultationRequestService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final NotifyService notifyService;

  @Autowired
  public ConfirmSatisfactoryApplicationService(PwaApplicationDetailService pwaApplicationDetailService,
                                               ConsultationRequestService consultationRequestService,
                                               EmailCaseLinkService emailCaseLinkService,
                                               NotifyService notifyService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.consultationRequestService = consultationRequestService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.notifyService = notifyService;
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

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        !isSatisfactory ? TaskTag.from(TaskStatus.NOT_STARTED) : TaskTag.from(TaskStatus.COMPLETED),
        taskState,
        task.getDisplayOrder());

  }

  /**
   * Task is accessible if the latest version of the application hasn't been confirmed satisfactory.
   */
  public boolean taskAccessible(PwaAppProcessingContext context) {
    return !isSatisfactory(context.getApplicationDetail());
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
          emailCaseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication())));
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

    notifyService.sendEmail(emailProps, recipient.getEmailAddress());
  }
}
