package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import java.time.Instant;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestDetail;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

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