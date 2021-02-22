package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Service
public class PrepareConsentTaskService implements AppProcessingService {

  private final DocumentService documentService;
  private final ApproveOptionsService approveOptionsService;

  @Autowired
  public PrepareConsentTaskService(DocumentService documentService,
                                   ApproveOptionsService approveOptionsService) {
    this.documentService = documentService;
    this.approveOptionsService = approveOptionsService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
  }


  public boolean taskAccessible(PwaAppProcessingContext processingContext) {
    return !getTaskStatus(processingContext).shouldForceInaccessible();
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

    var documentInProgress = documentService.getDocumentInstance(
        processingContext.getPwaApplication(),
        DocumentTemplateMnem.PWA_CONSENT_DOCUMENT
    ).isPresent();

    return documentInProgress ? TaskStatus.IN_PROGRESS : TaskStatus.NOT_STARTED;

  }


  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    var taskStatus = getTaskStatus(processingContext);

    var taskAccessible = !taskStatus.shouldForceInaccessible();

    return new TaskListEntry(
        task.getTaskName(),
        taskAccessible ? task.getRoute(processingContext) : null,
        TaskTag.from(taskStatus),
        task.getDisplayOrder());
  }
}
