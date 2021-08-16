package uk.co.ogauthority.pwa.service.appprocessing.context;


import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil;
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
        PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(pwaApplicationDetail.getPwaApplication()),
        Set.of());

  }

  public static PwaAppProcessingContext withPermissionsNoSatisfactoryVersions(PwaApplicationDetail pwaApplicationDetail,
                                                                              Set<PwaAppProcessingPermission> permissions) {

    return new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        permissions,
        null,
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(pwaApplicationDetail.getPwaApplication()), Set.of());

  }

  public static PwaAppProcessingContext withoutPermissions(PwaApplicationDetail pwaApplicationDetail) {

    return withAppInvolvement(
        pwaApplicationDetail, ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplicationDetail.getPwaApplication()));

  }

  public static PwaAppProcessingContext withAppInvolvement(PwaApplicationDetail pwaApplicationDetail, ApplicationInvolvementDto applicationInvolvement) {

    return new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        Set.of(),
        null,
        applicationInvolvement,
        Set.of());

  }

}