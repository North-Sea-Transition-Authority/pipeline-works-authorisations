package uk.co.ogauthority.pwa.testutils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.enums.PwaResourceType;

public class ConsentSearchItemTestUtils {

  private ConsentSearchItemTestUtils() {
    throw new AssertionError();
  }

  public static ConsentSearchItem createSearchItem(int pwaId,
                                                   String fieldNameOrOtherReference,
                                                   String holders,
                                                   Instant firstConsentTimestamp) {
    var item = new ConsentSearchItem();
    item.setPwaId(pwaId);
    item.setPwaReference("PWA/" + item.getPwaId());
    item.setFieldNameOrOtherReference(fieldNameOrOtherReference);
    item.setResourceType(PwaResourceType.PETROLEUM);
    item.setHolderNamesCsv(holders);
    item.setFirstConsentTimestamp(firstConsentTimestamp);
    item.setLatestConsentTimestamp(firstConsentTimestamp.plus(400, ChronoUnit.DAYS));
    item.setLatestConsentReference("C/" + pwaId);
    return item;
  }

}
