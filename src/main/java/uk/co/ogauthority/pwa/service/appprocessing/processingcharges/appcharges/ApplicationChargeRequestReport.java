package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.PaymentHeader;

/**
 * Class to capture the breakdown of an application submission fee.
 */
public final class ApplicationChargeRequestReport implements PaymentHeader<ApplicationChargeItem> {

  private final Instant requestedInstant;
  private final PersonId requestedBy;
  private final Instant lastUpdatedInstant;
  private final PersonId lastUpdatedPersonId;
  private final int totalPennies;
  private final String feeSummary;
  private final List<ApplicationChargeItem> applicationChargeItems;
  private final PwaAppChargeRequestStatus pwaAppChargeRequestStatus;
  private final String waivedReason;
  private final String cancelledReason;

  ApplicationChargeRequestReport(Instant requestedInstant,
                                 PersonId requestedBy,
                                 Instant lastUpdatedInstant,
                                 PersonId lastUpdatedPersonId,
                                 Integer totalPennies,
                                 String feeSummary,
                                 List<ApplicationChargeItem> applicationChargeItems,
                                 PwaAppChargeRequestStatus pwaAppChargeRequestStatus,
                                 String waivedReason,
                                 String cancelledReason) {
    this.requestedInstant = requestedInstant;
    this.requestedBy = requestedBy;
    this.lastUpdatedInstant = lastUpdatedInstant;
    this.lastUpdatedPersonId = lastUpdatedPersonId;
    this.totalPennies = totalPennies;
    this.feeSummary = feeSummary;
    this.applicationChargeItems = applicationChargeItems;
    this.pwaAppChargeRequestStatus = pwaAppChargeRequestStatus;
    this.waivedReason = waivedReason;
    this.cancelledReason = cancelledReason;
  }


  @Override
  public int getTotalPennies() {
    return totalPennies;
  }

  @Override
  public String getSummary() {
    return feeSummary;
  }

  @Override
  public List<ApplicationChargeItem> getPaymentItems() {
    return Collections.unmodifiableList(applicationChargeItems);
  }

  public PwaAppChargeRequestStatus getPwaAppChargeRequestStatus() {
    return pwaAppChargeRequestStatus;
  }

  public String getWaivedReason() {
    return waivedReason;
  }

  public String getCancelledReason() {
    return cancelledReason;
  }
}
