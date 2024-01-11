package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadFieldTestUtil {

  // no instantiation
  private PadFieldTestUtil() {
  }

  private static PadLinkedArea createPadField(PwaApplicationDetail pwaApplicationDetail){
    var f = new PadLinkedArea();
    f.setPwaApplicationDetail(pwaApplicationDetail);
    return f;
  }

  public static PadLinkedArea createManualPadField(PwaApplicationDetail pwaApplicationDetail){
    var f = createPadField(pwaApplicationDetail);
    // dont care about the name
    f.setAreaName(String.valueOf(RandomUtils.nextBytes(10)));
    return f;
  }

  public static PadLinkedArea createDevukPadField(PwaApplicationDetail pwaApplicationDetail, DevukField devukField){
    var f = createPadField(pwaApplicationDetail);
    f.setDevukField(devukField);
    return f;
  }
}
