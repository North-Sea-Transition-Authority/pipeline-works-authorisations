package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.appprocessing.appinvolvement.OpenConsentReview;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Service
public class PrepareConsentTaskService implements AppProcessingService {

  private final DocumentService documentService;
  private final ApproveOptionsService approveOptionsService;
  private final ConsentReviewService consentReviewService;

  @Autowired
  public PrepareConsentTaskService(DocumentService documentService,
                                   ApproveOptionsService approveOptionsService,
                                   ConsentReviewService consentReviewService) {
    this.documentService = documentService;
    this.approveOptionsService = approveOptionsService;
    this.consentReviewService = consentReviewService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
        || processingContext.getApplicationDetail().getStatus().equals(PwaApplicationStatus.COMPLETE);
  }


  public boolean taskAccessible(PwaAppProcessingContext processingContext) {

    var taskState = getTaskStateFromProcessingContextAndStatus(processingContext, getTaskStatus(processingContext));

    return !taskState.equals(TaskState.LOCK);
  }

  private TaskState getTaskStateFromProcessingContextAndStatus(PwaAppProcessingContext processingContext, TaskStatus taskStatus) {

    var taskStatusIsAccessible = !getTaskStatus(processingContext).shouldForceInaccessible();

    var appInvolvement = processingContext.getApplicationInvolvement();
    var appStatus = processingContext.getApplicationDetail().getStatus();

    // locked for industry & completed apps
    if (appInvolvement.hasOnlyIndustryInvolvement() || appStatus.equals(PwaApplicationStatus.COMPLETE)) {
      return TaskState.LOCK;
    }

    //assigned case officer should always be able to access task at case officer review stage
    if (appInvolvement.isUserAssignedCaseOfficer() && appStatus.equals(PwaApplicationStatus.CASE_OFFICER_REVIEW)) {
      return TaskState.EDIT;
      // locked for consent reviewer when review is not open, unlocked when open
    } else if (processingContext.hasProcessingPermission(PwaAppProcessingPermission.CONSENT_REVIEW)) {
      //TODO PWA-1243: is this correct, or is checking EDIT_CONSENT_DOCUMENT required as well when review not open?
      return appInvolvement.getOpenConsentReview() == OpenConsentReview.YES ? TaskState.EDIT : TaskState.LOCK;
      // for all other user types lock if consent review open
    } else if (appInvolvement.getOpenConsentReview() == OpenConsentReview.YES) {
      return TaskState.LOCK;
    }

    // if no special case encountered, use the status to decide if task is locked or not.
    return taskStatusIsAccessible ? TaskState.EDIT : TaskState.LOCK;

  }


  // helper to centralise logic around task status and task accessibility
  private TaskStatus getTaskStatus(PwaAppProcessingContext processingContext) {

    boolean optionsConfirmedIfApplicableCheck = true;
    if (PwaApplicationType.OPTIONS_VARIATION.equals(processingContext.getApplicationType())) {
      var optionsStatus = approveOptionsService.getOptionsApprovalStatus(processingContext.getApplicationDetail());
      optionsConfirmedIfApplicableCheck = optionsStatus.isConsentedOptionConfirmed();
    }

    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();

    if (!atLeastOneSatisfactoryVersion) {
      return TaskStatus.CANNOT_START_YET;
    }

    if (!optionsConfirmedIfApplicableCheck) {
      return TaskStatus.NOT_REQUIRED;
    }

    if (consentReviewService.isApplicationConsented(processingContext.getApplicationDetail())) {
      return TaskStatus.COMPLETED;
    }

    boolean documentInProgress = documentService
        .getDocumentInstance(processingContext.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT)
        .isPresent();

    return documentInProgress ? TaskStatus.IN_PROGRESS : TaskStatus.NOT_STARTED;

  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    var taskStatus = getTaskStatus(processingContext);

    var taskState = getTaskStateFromProcessingContextAndStatus(processingContext, taskStatus);

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        TaskTag.from(taskStatus),
        taskState,
        task.getDisplayOrder());

  }

}
