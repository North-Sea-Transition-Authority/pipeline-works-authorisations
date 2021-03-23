package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.feeproviders.ApplicationFeeItem;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.feeproviders.ApplicationFeeItemTestUtil;

public class ApplicationFeeReportTestUtil {
  private ApplicationFeeReportTestUtil(){
    throw new UnsupportedOperationException("No util for you!");
  }


  public static ApplicationFeeReport createReport(PwaApplication pwaApplication,
                                                  Integer totalPennies,
                                                  String feeSummary,
                                                  List<ApplicationFeeItem> applicationFeeItems){

    return new ApplicationFeeReport(
        pwaApplication,
        totalPennies,
        feeSummary,
        applicationFeeItems
    );
  }

  public static ApplicationFeeItem createApplicationFeeItem(String desc, int amount){
    return ApplicationFeeItemTestUtil.createAppFeeItem(desc, amount);

  }

}