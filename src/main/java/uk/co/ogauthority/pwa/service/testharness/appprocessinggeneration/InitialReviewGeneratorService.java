package uk.co.ogauthority.pwa.service.testharness.appprocessinggeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewPaymentDecision;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;

@Service
@Profile("test-harness")
class InitialReviewGeneratorService implements TestHarnessAppProcessingService {

  private final InitialReviewService initialReviewService;

  private static final PwaAppProcessingTask LINKED_APP_PROCESSING_TASK = PwaAppProcessingTask.INITIAL_REVIEW;

  @Autowired
  public InitialReviewGeneratorService(
      InitialReviewService initialReviewService) {
    this.initialReviewService = initialReviewService;
  }


  @Override
  public PwaAppProcessingTask getLinkedAppProcessingTask() {
    return LINKED_APP_PROCESSING_TASK;
  }


  @Override
  public void generateAppProcessingTaskData(TestHarnessAppProcessingProperties appProcessingProps) {
    initialReviewService.acceptApplication(
        appProcessingProps.getPwaApplicationDetail(),
        appProcessingProps.getCaseOfficerAua().getLinkedPerson().getId(),
        appProcessingProps.getInitialReviewPaymentDecision(),
        InitialReviewPaymentDecision.PAYMENT_WAIVED.equals(appProcessingProps.getInitialReviewPaymentDecision())
            ? "Waiving payment for test harness generated application" : null,
        appProcessingProps.getApplicantAua());
  }

}
