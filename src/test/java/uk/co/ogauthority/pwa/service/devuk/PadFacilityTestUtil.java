package uk.co.ogauthority.pwa.service.devuk;

import java.util.Arrays;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadFacilityTestUtil {
  private PadFacilityTestUtil() {
    // no instantiation
  }

  public static PadFacility createManualFacility(PwaApplicationDetail pwaApplicationDetail) {

    var randomString = Arrays.toString(RandomUtils.nextBytes(10));
    var pf = new PadFacility();
    pf.setPwaApplicationDetail(pwaApplicationDetail);
    pf.setFacilityNameManualEntry(randomString);
    ObjectTestUtils.assertAllFieldsNotNull(
        pf,
        PadFacility.class,
        Set.of(PadFacility_.ID, PadFacility_.FACILITY));
    return pf;

  }

  public static PadFacility createDevukLinkedFacility(PwaApplicationDetail pwaApplicationDetail, DevukFacility devukFacility) {

    var pf = new PadFacility();
    pf.setPwaApplicationDetail(pwaApplicationDetail);
    pf.setFacility(devukFacility);
    ObjectTestUtils.assertAllFieldsNotNull(
        pf,
        PadFacility.class,
        Set.of(PadFacility_.ID, PadFacility_.FACILITY_NAME_MANUAL_ENTRY));
    return pf;

  }
}
