package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask.distinctByService;

import jakarta.transaction.Transactional;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTaskService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class PwaApplicationDetailVersioningService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ApplicationTaskService applicationTaskService;

  @Autowired
  public PwaApplicationDetailVersioningService(PwaApplicationDetailService pwaApplicationDetailService,
                                               ApplicationTaskService applicationTaskService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.applicationTaskService = applicationTaskService;
  }


  @Transactional
  public PwaApplicationDetail createNewApplicationVersion(PwaApplicationDetail detail,
                                                          WebUserAccount webUserAccount) {

    var newTipDetail = pwaApplicationDetailService
        .createNewTipDetail(detail, PwaApplicationStatus.UPDATE_REQUESTED, webUserAccount);

    ApplicationTask.stream()
        .filter(applicationTask -> applicationTaskService.taskAllowsCopySectionInformation(applicationTask, detail))
        .filter(distinctByService())
        .sorted(Comparator.comparing(ApplicationTask::getVersioningProcessingOrder))
        .forEachOrdered(applicationTask -> applicationTaskService.copyApplicationTaskDataToApplicationDetail(
            applicationTask,
            detail,
            newTipDetail));

    return newTipDetail;

  }

}
