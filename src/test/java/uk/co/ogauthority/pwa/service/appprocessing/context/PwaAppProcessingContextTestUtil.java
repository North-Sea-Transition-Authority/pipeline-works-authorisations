package uk.co.ogauthority.pwa.service.appprocessing.context;


import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;

public class PwaAppProcessingContextTestUtil {

  private PwaAppProcessingContextTestUtil() {
    // no instantiation
  }

  public static PwaAppProcessingContext withPermissions(PwaApplicationDetail pwaApplicationDetail,
                                                        Set<PwaAppProcessingPermission> permissions) {

    return new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        permissions,
        null,
        null);

  }

}