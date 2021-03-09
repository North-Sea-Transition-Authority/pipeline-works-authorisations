package uk.co.ogauthority.pwa.service.appprocessing.processingcharges;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;

@Service
public class CancelPaymentRequestAppProcessingService implements AppProcessingService {

  private final ApplicationChargeRequestService applicationChargeRequestService;

  @Autowired
  public CancelPaymentRequestAppProcessingService(ApplicationChargeRequestService applicationChargeRequestService) {
    this.applicationChargeRequestService = applicationChargeRequestService;
  }

  public boolean taskAccessible(PwaAppProcessingContext processingContext) {
    return processingContext.hasProcessingPermission(PwaAppProcessingPermission.CANCEL_PAYMENT)
        && applicationChargeRequestService.applicationHasOpenChargeRequest(processingContext.getPwaApplication());
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return taskAccessible(processingContext);
  }

}
