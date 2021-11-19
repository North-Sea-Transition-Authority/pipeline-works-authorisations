package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders;

public final class ApplicationFeeItemTestUtil {

  private ApplicationFeeItemTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }

  public static ApplicationFeeItem createAppFeeItem(String desc, int pennyAmount){
    return new ApplicationFeeItem(desc, pennyAmount);
  }
}