package uk.co.ogauthority.pwa.features.application.authorisation.context;

import java.util.Set;
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
   * If the application status matches one of the required ones, pass, otherwise throw relevant exception.
   */
  public static void performAppStatusCheck(Set<PwaApplicationStatus> expectedStatuses,
                                           PwaApplicationDetail pwaApplicationDetail) {

    if (!expectedStatuses.isEmpty() && !expectedStatuses.contains(pwaApplicationDetail.getStatus())) {
      throw new PwaEntityNotFoundException(
          String.format("PwaApplicationDetailId:%s Did not have expected status:%s. Actual status:%s",
              pwaApplicationDetail.getId(),
              expectedStatuses,
              pwaApplicationDetail.getStatus()
          )
      );
    }

  }

}
