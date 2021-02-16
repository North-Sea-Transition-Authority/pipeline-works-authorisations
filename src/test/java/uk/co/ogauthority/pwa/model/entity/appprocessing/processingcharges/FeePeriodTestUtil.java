package uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FeePeriodTestUtil {

  private FeePeriodTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }


  public static FeePeriodDetail createDefaultFeePeriodDetail(){

    var feePeriod = new FeePeriod();
    feePeriod.setId(1);
    feePeriod.setDescription("Test Fee Period");

    return createTipFeePeriodDetail(
        feePeriod,
        LocalDateTime.of(2020, 1, 1, 0 ,0,0).toInstant(ZoneOffset.UTC)
    );

  }

  public static FeePeriodDetail createTipFeePeriodDetail(FeePeriod feePeriod, Instant periodStart){
    var fpd = new FeePeriodDetail();
    fpd.setId(1);
    fpd.setTipFlag(true);
    fpd.setPeriodStartTimestamp(periodStart);

    return fpd;
  }

  public static FeePeriodDetailFeeItem createFeePeriodFeeItem(FeePeriodDetail feePeriodDetail, String desc, int amount){
    var fi  = new FeeItem();
    fi.setDisplayDescription(desc);
    var fpfi = new FeePeriodDetailFeeItem();
    fpfi.setFeePeriodDetail(feePeriodDetail);
    fpfi.setFeeItem(fi);
    fpfi.setPennyAmount(amount);
    return fpfi;
  }
}