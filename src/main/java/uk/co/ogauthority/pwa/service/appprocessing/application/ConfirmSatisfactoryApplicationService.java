package uk.co.ogauthority.pwa.service.appprocessing.application;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class ConfirmSatisfactoryApplicationService implements AppProcessingService {

  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public ConfirmSatisfactoryApplicationService(PwaApplicationDetailService pwaApplicationDetailService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CONFIRM_SATISFACTORY_APPLICATION)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    boolean isSatisfactory = isSatisfactory(processingContext.getApplicationDetail());

    return new TaskListEntry(
        task.getTaskName(),
        !isSatisfactory ? task.getRoute(processingContext) : null,
        !isSatisfactory ? TaskTag.from(TaskStatus.NOT_COMPLETED) : TaskTag.from(TaskStatus.COMPLETED),
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

  @Transactional
  public void confirmSatisfactory(PwaApplicationDetail applicationDetail,
                                  String reason,
                                  Person confirmingPerson) {

    if (isSatisfactory(applicationDetail)) {
      throw new IllegalStateException(String.format(
          "Cannot confirm app detail satisfactory as it is already satisfactory. pad_id [%s]", applicationDetail.getId()));
    }

    pwaApplicationDetailService.setConfirmedSatisfactoryData(applicationDetail, reason, confirmingPerson);

  }
}
