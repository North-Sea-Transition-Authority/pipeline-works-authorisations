package uk.co.ogauthority.pwa.model.entity.pwaapplications.search;

import java.time.Instant;

public class PadVersionLookupTestUtil {

  private PadVersionLookupTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }

  public static PadVersionLookup createLookupForDraftOnlyApp(int appId) {

    var padVersionLookup = new PadVersionLookup();
    padVersionLookup.setPwaApplicationId(appId);
    padVersionLookup.setMaxDraftVersionNo(1);
    return padVersionLookup;
  }

  public static PadVersionLookup createLookupForSubmittedApp(int appId,
                                                             Integer lastSubmittedVersion,
                                                             Integer maxDraftVersion,
                                                             Instant lastSatisfactoryInstant) {
    var padVersionLookup = new PadVersionLookup();
    padVersionLookup.setPwaApplicationId(appId);
    padVersionLookup.setLatestSubmittedVersionNo(lastSubmittedVersion);
    padVersionLookup.setMaxDraftVersionNo(maxDraftVersion);
    padVersionLookup.setLatestConfirmedSatisfactoryTimestamp(lastSatisfactoryInstant);
    return padVersionLookup;
  }

}