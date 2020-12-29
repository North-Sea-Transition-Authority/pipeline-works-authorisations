package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import java.util.Comparator;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class PwaApplicationDetailVersioningService {

  private final TaskListService taskListService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ApplicationTaskService applicationTaskService;

  @Autowired
  public PwaApplicationDetailVersioningService(TaskListService taskListService,
                                               PwaApplicationDetailService pwaApplicationDetailService,
                                               ApplicationTaskService applicationTaskService) {
    this.taskListService = taskListService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.applicationTaskService = applicationTaskService;
  }


  @Transactional
  public PwaApplicationDetail createNewApplicationVersion(PwaApplicationDetail detail,
                                                          WebUserAccount webUserAccount) {
    var newTipDetail = pwaApplicationDetailService.createNewTipDetail(detail, webUserAccount);

    ApplicationTask.stream()
        .filter(applicationTask -> applicationTaskService.taskAllowsCopySectionInformation(applicationTask, detail))
        .sorted(Comparator.comparing(ApplicationTask::getVersioningProcessingOrder))
        .forEachOrdered(applicationTask -> applicationTaskService.copyApplicationTaskDataToApplicationDetail(
            applicationTask,
            detail,
            newTipDetail));

    return newTipDetail;

  }

}
