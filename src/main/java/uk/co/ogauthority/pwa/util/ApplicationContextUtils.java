package uk.co.ogauthority.pwa.util;

import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

/**
 * Utility class to provide common functionality when building up PWA application contexts.
 */
public class ApplicationContextUtils {

  private ApplicationContextUtils() {
    throw new AssertionError();
  }

  /**
   * If the application status matches the required one, pass, otherwise throw relevant exception.
   */
  public static void performAppStatusCheck(PwaApplicationStatus expectedStatus,
                                           PwaApplicationDetail pwaApplicationDetail) {

    if (expectedStatus != null && !expectedStatus.equals(pwaApplicationDetail.getStatus())) {
      throw new PwaEntityNotFoundException(
          String.format("PwaApplicationDetailId:%s Did not have expected status:%s. Actual status:%s",
              pwaApplicationDetail.getId(),
              expectedStatus,
              pwaApplicationDetail.getStatus()
          )
      );
    }

  }

}
