package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadEnvironmentalDecommissioningTestUtil {

  private PadEnvironmentalDecommissioningTestUtil() {
    // no instantiation
  }

  public static PadEnvironmentalDecommissioning createPadEnvironmentalDecommissioning(
      PwaApplicationDetail pwaApplicationDetail){

    var entity = new PadEnvironmentalDecommissioning();
    entity.setPwaApplicationDetail(pwaApplicationDetail);
    entity.setTransboundaryEffect(true);
    entity.setEmtHasSubmittedPermits(true);
    entity.setPermitsSubmitted("Submitted permits");
    entity.setEmtHasOutstandingPermits(true);
    entity.setPermitsPendingSubmission("Pending permits");
    entity.setEmtSubmissionTimestamp(Instant.now());
    entity.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    entity.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));

    ObjectTestUtils.assertAllFieldsNotNull(
        entity,
        PadEnvironmentalDecommissioning.class,
        Set.of(PadEnvironmentalDecommissioning_.ID));
    return entity;

  }
}
