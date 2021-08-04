package uk.co.ogauthority.pwa.service.testharness.appprocessinggeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.appprocessing.application.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;

@Service
@Profile("test-harness")
class ConfirmSatisfactoryGeneratorService implements TestHarnessAppProcessingService {

  private final ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;

  private static final PwaAppProcessingTask LINKED_APP_PROCESSING_TASK = PwaAppProcessingTask.CONFIRM_SATISFACTORY_APPLICATION;

  @Autowired
  public ConfirmSatisfactoryGeneratorService(
      ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService) {
    this.confirmSatisfactoryApplicationService = confirmSatisfactoryApplicationService;
  }


  @Override
  public PwaAppProcessingTask getLinkedAppProcessingTask() {
    return LINKED_APP_PROCESSING_TASK;
  }


  @Override
  public void generateAppProcessingTaskData(TestHarnessAppProcessingProperties appProcessingProps) {
    confirmSatisfactoryApplicationService.confirmSatisfactory(
        appProcessingProps.getPwaApplicationDetail(),
        "Confirming test harness application satisfactory reason",
        appProcessingProps.getCaseOfficerAua().getLinkedPerson());
  }


}
