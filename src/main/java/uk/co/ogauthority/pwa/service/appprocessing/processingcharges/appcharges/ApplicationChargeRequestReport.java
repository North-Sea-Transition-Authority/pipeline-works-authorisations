package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.PaymentHeader;

/**
 * Class to capture the breakdown of an application submission fee.
 */
public final class ApplicationChargeRequestReport implements PaymentHeader<ApplicationChargeItem> {

  private final Instant requestedInstant;
  private final PersonId requestedByPersonId;
  private final Instant lastUpdatedInstant;
  private final PersonId lastUpdatedPersonId;
  private final Instant paidInstant;
  private final PersonId paidByPersonId;
  private final int totalPennies;
  private final String feeSummary;
  private final List<ApplicationChargeItem> applicationChargeItems;
  private final PwaAppChargeRequestStatus pwaAppChargeRequestStatus;
  private final String waivedReason;
  private final String cancelledReason;

  ApplicationChargeRequestReport(Instant requestedInstant,
                                 PersonId requestedByPersonId,
                                 Instant lastUpdatedInstant,
                                 PersonId lastUpdatedPersonId,

                                 Instant paidInstant,
                                 PersonId paidByPersonId,
                                 Integer totalPennies,
                                 String feeSummary,
                                 List<ApplicationChargeItem> applicationChargeItems,
                                 PwaAppChargeRequestStatus pwaAppChargeRequestStatus,
                                 String waivedReason,
                                 String cancelledReason) {
    this.requestedInstant = requestedInstant;
    this.requestedByPersonId = requestedByPersonId;
    this.lastUpdatedInstant = lastUpdatedInstant;
    this.lastUpdatedPersonId = lastUpdatedPersonId;
    this.paidInstant = paidInstant;
    this.paidByPersonId = paidByPersonId;
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

  public Instant getRequestedInstant() {
    return requestedInstant;
  }

  public PersonId getRequestedByPersonId() {
    return requestedByPersonId;
  }

  public Instant getLastUpdatedInstant() {
    return lastUpdatedInstant;
  }

  public PersonId getLastUpdatedPersonId() {
    return lastUpdatedPersonId;
  }

  public Instant getPaidInstant() {
    return paidInstant;
  }

  public PersonId getPaidByPersonId() {
    return paidByPersonId;
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
