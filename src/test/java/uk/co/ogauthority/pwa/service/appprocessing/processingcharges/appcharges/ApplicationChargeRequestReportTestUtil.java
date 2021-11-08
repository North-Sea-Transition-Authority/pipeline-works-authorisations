package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import java.time.Instant;
import java.util.List;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;

public class ApplicationChargeRequestReportTestUtil {

  private ApplicationChargeRequestReportTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }


  public static ApplicationChargeRequestReport createOpenReport(Integer totalPennies,
                                                                String summary,
                                                                List<ApplicationChargeItem> applicationFeeItems) {

    return createOpenReport(totalPennies, summary, applicationFeeItems, Instant.now(), new PersonId(1));
  }

  public static ApplicationChargeRequestReport createOpenReport(Integer totalPennies,
                                                                String summary,
                                                                Instant requestTime,
                                                                PersonId requestPersonId,
                                                                List<ApplicationChargeItem> applicationFeeItems) {

    return createOpenReport(totalPennies, summary, applicationFeeItems, requestTime, requestPersonId);
  }

  public static ApplicationChargeRequestReport createOpenReport(Integer totalPennies,
                                                                String summary,
                                                                List<ApplicationChargeItem> applicationFeeItems,
                                                                Instant requestedInstant,
                                                                PersonId requestedBy) {

    return new ApplicationChargeRequestReport(
        requestedInstant,
        requestedBy,
        requestedInstant,
        requestedBy,
        null,
        null,
        totalPennies,
        summary,
        applicationFeeItems,
        PwaAppChargeRequestStatus.OPEN,
        null,
        null
    );
  }


  public static ApplicationChargeRequestReport createCancelledReport(Integer totalPennies,
                                                                     String summary,
                                                                     Instant requestedInstant,
                                                                     PersonId requestedBy,
                                                                     Instant cancelledInstant,
                                                                     PersonId cancelledBy) {

    return new ApplicationChargeRequestReport(
        requestedInstant,
        requestedBy,
        cancelledInstant,
        cancelledBy,
        null,
        null,
        totalPennies,
        summary,
        List.of(),
        PwaAppChargeRequestStatus.CANCELLED,
        null,
        "CANCEL_REASON"
    );
  }

  public static ApplicationChargeRequestReport createWaivedReport(Integer totalPennies,
                                                                  String summary,
                                                                  Instant requestedInstant,
                                                                  PersonId requestedBy) {

    return new ApplicationChargeRequestReport(
        requestedInstant,
        requestedBy,
        requestedInstant,
        requestedBy,
        null,
        null,
        totalPennies,
        summary,
        List.of(),
        PwaAppChargeRequestStatus.WAIVED,
        "WAIVE_REASON",
        null
    );
  }

  public static ApplicationChargeRequestReport createPaidReport(Integer totalPennies,
                                                                String summary,
                                                                Instant requestedInstant,
                                                                PersonId requestedBy,
                                                                Instant paidInstant,
                                                                PersonId paidBy) {

    return new ApplicationChargeRequestReport(
        requestedInstant,
        requestedBy,
        requestedInstant,
        requestedBy,
        paidInstant,
        paidBy,
        totalPennies,
        summary,
        List.of(),
        PwaAppChargeRequestStatus.PAID,
        null,
        null
    );
  }


  public static ApplicationChargeItem createApplicationChargeItem(String desc, int amount) {
    return new ApplicationChargeItem(desc, amount);
  }

}