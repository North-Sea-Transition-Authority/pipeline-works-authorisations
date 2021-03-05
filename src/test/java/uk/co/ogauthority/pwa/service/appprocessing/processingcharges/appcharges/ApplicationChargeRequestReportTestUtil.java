package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import java.time.Instant;
import java.util.List;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;

public class ApplicationChargeRequestReportTestUtil {

  private ApplicationChargeRequestReportTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }


  public static ApplicationChargeRequestReport createOpenReport(Integer totalPennies,
                                                                String summary,
                                                                List<ApplicationChargeItem> applicationFeeItems) {

    return new ApplicationChargeRequestReport(
        Instant.now(),
        new PersonId(1),
        Instant.now(),
        new PersonId(1),
        totalPennies,
        summary,
        applicationFeeItems,
        PwaAppChargeRequestStatus.OPEN,
        null,
        null
    );
  }

  public static ApplicationChargeItem createApplicationChargeItem(String desc, int amount) {
    return new ApplicationChargeItem(desc, amount);
  }

}