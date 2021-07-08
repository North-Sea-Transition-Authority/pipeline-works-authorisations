package uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.ConsultationService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

/**
 * A service to check app processing tasks for issues and produce warning information when the tasks should be started.
 */
@Service
public class AppProcessingTaskWarningService {

  private final ConsultationService consultationService;
  private final PublicNoticeService publicNoticeService;


  @Autowired
  public AppProcessingTaskWarningService(
      ConsultationService consultationService,
      PublicNoticeService publicNoticeService) {
    this.consultationService = consultationService;
    this.publicNoticeService = publicNoticeService;
  }


  public NonBlockingTasksWarning getNonBlockingTasksWarning(PwaApplication pwaApplication) {

    var consultationsMissing = consultationService.consultationsTaskRequired(pwaApplication)
        && consultationService.getTaskStatus(pwaApplication).equals(TaskStatus.NOT_STARTED);
    var publicNoticesMissing = publicNoticeService.publicNoticeTaskRequired(pwaApplication)
        && !publicNoticeService.publicNoticeTaskStarted(pwaApplication);

    String incompleteTasksWarningText = null;
    if (consultationsMissing && publicNoticesMissing) {
      incompleteTasksWarningText = "The consultations and public notice tasks have not been started";
    } else if (consultationsMissing) {
      incompleteTasksWarningText = "The consultations task has not been started";
    } else if (publicNoticesMissing) {
      incompleteTasksWarningText = "The public notice task has not been started";
    }

    return new NonBlockingTasksWarning(consultationsMissing || publicNoticesMissing,
        incompleteTasksWarningText,
        CaseManagementUtils.routeCaseManagement(pwaApplication));

  }




}
