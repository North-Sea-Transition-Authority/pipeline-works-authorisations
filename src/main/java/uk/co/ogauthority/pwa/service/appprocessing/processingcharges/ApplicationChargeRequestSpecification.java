package uk.co.ogauthority.pwa.service.appprocessing.processingcharges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

/**
 * Class to capture the arguments required to generate charge request for a pwa application.
 */
public final class ApplicationChargeRequestSpecification {

  private PwaApplication pwaApplication;
  private PwaAppChargeRequestStatus pwaAppChargeRequestStatus;
  private PersonId onPaymentCompleteCaseOfficerPersonId;
  private Integer totalPennies;
  private String chargeSummary;
  private String chargeWaivedReason;
  private List<ApplicationChargeItem> applicationChargeItems;


  public ApplicationChargeRequestSpecification(PwaApplication pwaApplication,
                                               PwaAppChargeRequestStatus pwaAppChargeRequestStatus) {
    if (!ObjectUtils.allNotNull(pwaApplication, pwaAppChargeRequestStatus)) {
      throw new UnsupportedOperationException("All constructor arguments must have a value");
    }

    this.pwaApplication = pwaApplication;
    this.pwaAppChargeRequestStatus = pwaAppChargeRequestStatus;
    applicationChargeItems = new ArrayList<>();
  }

  public ApplicationChargeRequestSpecification setTotalPennies(Integer totalPennies) {
    this.totalPennies = totalPennies;
    return this;
  }

  public ApplicationChargeRequestSpecification setChargeSummary(String chargeSummary) {
    this.chargeSummary = chargeSummary;
    return this;
  }

  public ApplicationChargeRequestSpecification setChargeWaivedReason(String chargeWaivedReason) {
    // no validation - allows removing/changing of set reason
    this.chargeWaivedReason = chargeWaivedReason;
    return this;
  }

  public ApplicationChargeRequestSpecification addChargeItem(String description, int pennyAmount) {
    this.applicationChargeItems.add(
        new ApplicationChargeItem(description, pennyAmount)
    );
    return this;
  }

  public ApplicationChargeRequestSpecification setOnPaymentCompleteCaseOfficerPersonId(
      PersonId onPaymentCompleteCaseOfficerPersonId) {
    this.onPaymentCompleteCaseOfficerPersonId = onPaymentCompleteCaseOfficerPersonId;
    return this;
  }

  public List<ApplicationChargeItem> getApplicationChargeItems() {
    return Collections.unmodifiableList(applicationChargeItems);
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public PwaAppChargeRequestStatus getPwaAppChargeRequestStatus() {
    return pwaAppChargeRequestStatus;
  }

  public Integer getTotalPennies() {
    return totalPennies;
  }

  public String getChargeSummary() {
    return chargeSummary;
  }

  public String getChargeWaivedReason() {
    return chargeWaivedReason;
  }

  public PersonId getOnPaymentCompleteCaseOfficerPersonId() {
    return onPaymentCompleteCaseOfficerPersonId;
  }

}
