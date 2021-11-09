package uk.co.ogauthority.pwa.service.appprocessing.context;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

public class PwaAppProcessingContextTest {

  private PwaApplicationDetail appDetail;

  @Before
  public void setup(){
    appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void hasAnyProcessingPermission_whenAllSearchPermissionsInContext() {
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
  public void hasAnyProcessingPermission_whenSomeSearchPermissionsInContext() {
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
  public void hasAnyProcessingPermission_whenNoSearchPermissionsInContext() {
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