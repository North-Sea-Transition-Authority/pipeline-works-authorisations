package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;

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
