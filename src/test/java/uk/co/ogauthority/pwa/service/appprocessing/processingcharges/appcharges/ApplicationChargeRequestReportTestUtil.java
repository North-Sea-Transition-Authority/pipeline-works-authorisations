package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

public class ApplicationChargeRequestReportTestUtil {

  private ApplicationChargeRequestReportTestUtil(){
    throw new UnsupportedOperationException("No util for you!");
  }


  public static ApplicationChargeRequestReport createOpenReport(PwaApplication pwaApplication,
                                                  Integer totalPennies,
                                                  String summary,
                                                  List<ApplicationChargeItem> applicationFeeItems){

    return new ApplicationChargeRequestReport(
        pwaApplication,
        totalPennies,
        summary,
        applicationFeeItems,
        PwaAppChargeRequestStatus.OPEN,
        null
    );
  }

  public static ApplicationChargeItem createApplicationChargeItem(String desc, int amount){
    return new ApplicationChargeItem(desc, amount);

  }

}