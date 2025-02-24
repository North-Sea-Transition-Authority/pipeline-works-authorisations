package uk.co.ogauthority.pwa.features.appprocessing.authorisation.context;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

class PwaAppProcessingContextTest {

  private PwaApplicationDetail appDetail;

  @BeforeEach
  void setup(){
    appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  void hasAnyProcessingPermission_whenAllSearchPermissionsInContext() {
    var context = PwaAppProcessingContextTestUtil.withPermissions(
        appDetail,
        EnumSet.of(
            PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE, PwaAppProcessingPermission.ADD_CASE_NOTE
        )
    );

    assertThat(context.hasAnyProcessingPermission(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE, PwaAppProcessingPermission.ADD_CASE_NOTE))
        .isTrue();
  }

  @Test
  void hasAnyProcessingPermission_whenSomeSearchPermissionsInContext() {
    var context = PwaAppProcessingContextTestUtil.withPermissions(
        appDetail,
        EnumSet.of(
            PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE, PwaAppProcessingPermission.ADD_CASE_NOTE
        )
    );

    assertThat(context.hasAnyProcessingPermission(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE))
        .isTrue();
  }

  @Test
  void hasAnyProcessingPermission_whenNoSearchPermissionsInContext() {
    var context = PwaAppProcessingContextTestUtil.withPermissions(
        appDetail,
        EnumSet.of(
            PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE, PwaAppProcessingPermission.ADD_CASE_NOTE
        )
    );

    assertThat(context.hasAnyProcessingPermission(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY))
        .isFalse();
  }
}