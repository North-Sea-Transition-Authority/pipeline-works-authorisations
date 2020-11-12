package uk.co.ogauthority.pwa.service.appprocessing.decision;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.documents.DocumentService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;

@Service
public class ApplicationDecisionService implements AppProcessingService {

  private final DocumentService documentService;

  @Autowired
  public ApplicationDecisionService(DocumentService documentService) {
    this.documentService = documentService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    var taskStatus = documentService.getDocumentInstance(processingContext.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT)
        .map(i -> TaskStatus.IN_PROGRESS)
        .orElse(TaskStatus.NOT_STARTED);

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        TaskTag.from(taskStatus),
        task.getDisplayOrder());

  }
}
