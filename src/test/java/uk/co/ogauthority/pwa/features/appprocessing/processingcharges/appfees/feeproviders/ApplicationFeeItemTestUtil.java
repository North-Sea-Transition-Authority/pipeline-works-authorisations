package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders;

import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;

public final class ApplicationFeeItemTestUtil {

  private ApplicationFeeItemTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }

  public static ApplicationFeeItem createAppFeeItem(PwaApplicationFeeType pwaApplicationFeeType, String desc, int pennyAmount) {
    return new ApplicationFeeItem(pwaApplicationFeeType, desc, pennyAmount);
  }
}