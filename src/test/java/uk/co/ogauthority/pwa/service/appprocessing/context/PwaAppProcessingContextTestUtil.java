package uk.co.ogauthority.pwa.service.appprocessing.context;


import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(pwaApplicationDetail.getPwaApplication()));

  }

  public static PwaAppProcessingContext withPermissionsNoSatisfactoryVersions(PwaApplicationDetail pwaApplicationDetail,
                                                                              Set<PwaAppProcessingPermission> permissions) {

    return new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        permissions,
        null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(pwaApplicationDetail.getPwaApplication()));

  }

}