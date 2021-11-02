package uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges;

import java.time.Instant;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

public final class PwaAppChargeRequestTestUtil {

  private PwaAppChargeRequestTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }


  public static PwaAppChargeRequestDetail createDefaultChargeRequest(PwaApplication pwaApplication,
                                                                     Person person,
                                                                     PwaAppChargeRequestStatus pwaAppChargeRequestStatus) {
    var chargeRequest = new PwaAppChargeRequest();
    chargeRequest.setPwaApplication(pwaApplication);
    chargeRequest.setRequestedByPersonId(person.getId());
    chargeRequest.setRequestedByTimestamp(Instant.now());

    var detail = new PwaAppChargeRequestDetail(
        chargeRequest
    );
    detail.setTipFlag(true);
    detail.setChargeSummary("Charge Summary");
    detail.setTotalPennies(100);
    detail.setPwaAppChargeRequestStatus(pwaAppChargeRequestStatus);

    return detail;


  }


}