package uk.co.ogauthority.pwa.service.applicationsummariser;

import java.util.function.Function;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class ApplicationSummariserUtil {

  /**
   * Basic optimisation utility for common requirement of checking if 2 app details should be summarised.
   * if they are both the same app detail, then we dont need to do the check twice.
   */
  public static boolean canSummariseOptimised(PwaApplicationDetail newApplicationDetail,
                                              PwaApplicationDetail oldPwaApplicationDetail,
                                              Function<PwaApplicationDetail, Boolean> canSummariseCheckFunction) {
    var canSummariseNewAppDetail = canSummariseCheckFunction.apply(newApplicationDetail);
    if (newApplicationDetail.getId().equals(oldPwaApplicationDetail.getId())) {
      return canSummariseNewAppDetail;
    }

    return canSummariseNewAppDetail || canSummariseCheckFunction.apply(oldPwaApplicationDetail);
  }
}
