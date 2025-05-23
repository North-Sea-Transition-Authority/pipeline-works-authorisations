package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.appinvolvement.OpenConsentReview;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Service
public class PrepareConsentTaskService implements AppProcessingService {

  private final DocumentService documentService;
  private final ApproveOptionsService approveOptionsService;
  private final ConsentReviewService consentReviewService;

  private final PadPipelineTransferService pipelineTransferService;

  @Autowired
  public PrepareConsentTaskService(DocumentService documentService,
                                   ApproveOptionsService approveOptionsService,
                                   ConsentReviewService consentReviewService,
                                   PadPipelineTransferService pipelineTransferService) {
    this.documentService = documentService;
    this.approveOptionsService = approveOptionsService;
    this.consentReviewService = consentReviewService;
    this.pipelineTransferService = pipelineTransferService;
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

    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();

    // locked for industry & completed apps
    if (appInvolvement.hasOnlyIndustryInvolvement() || appStatus.equals(PwaApplicationStatus.COMPLETE)) {
      return TaskState.LOCK;
    }

    var detail = processingContext.getApplicationDetail();
    boolean isOptions = detail.getPwaApplicationType() == PwaApplicationType.OPTIONS_VARIATION;
    boolean optionsApprovedAndWorkDonePerOption = approveOptionsService.getOptionsApprovalStatus(detail).isConsentedOptionConfirmed();

    boolean optionsAppAndOptionsNotApprovedOrWorkNotDonePerOption = isOptions && !optionsApprovedAndWorkDonePerOption;

    // can only enter the task if work done per approved options
    if (optionsAppAndOptionsNotApprovedOrWorkNotDonePerOption) {
      return TaskState.LOCK;
    }

    /* assigned case officer only has access  to task at case officer review stage if there is at least one
    satisfactory version of the application*/
    if (appInvolvement.isUserAssignedCaseOfficer()
        && appStatus.equals(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        && atLeastOneSatisfactoryVersion) {
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

    var transfers = pipelineTransferService.findByRecipientApplication(processingContext.getApplicationDetail());
    for (var transfer : transfers) {
      var transferStatus = transfer.getDonorApplicationDetail().getStatus();
      if (transferStatus != PwaApplicationStatus.COMPLETE) {
        return TaskStatus.AWAITING_TRANSFER_COMPLETION;
      }
    }

    if (!optionsConfirmedIfApplicableCheck) {
      return TaskStatus.NOT_REQUIRED;
    }

    if (consentReviewService.isApplicationConsented(processingContext.getApplicationDetail())) {
      return TaskStatus.COMPLETED;
    }

    var docMnem = DocumentTemplateMnem.getMnemFromResourceType(processingContext.getApplicationDetail().getResourceType());
    boolean documentInProgress = documentService
        .getDocumentInstance(processingContext.getPwaApplication(), docMnem)
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
