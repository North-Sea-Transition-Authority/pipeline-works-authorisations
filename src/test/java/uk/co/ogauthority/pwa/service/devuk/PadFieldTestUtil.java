package uk.co.ogauthority.pwa.service.devuk;

import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadFieldTestUtil {

  // no instantiation
  private PadFieldTestUtil() {
  }

  private static PadField createPadField(PwaApplicationDetail pwaApplicationDetail){
    var f = new PadField();
    f.setPwaApplicationDetail(pwaApplicationDetail);
    return f;
  }

  public static PadField createManualPadField(PwaApplicationDetail pwaApplicationDetail){
    var f = createPadField(pwaApplicationDetail);
    // dont care about the name
    f.setFieldName(String.valueOf(RandomUtils.nextBytes(10)));
    return f;
  }

  public static PadField createDevukPadField(PwaApplicationDetail pwaApplicationDetail, DevukField devukField){
    var f = createPadField(pwaApplicationDetail);
    f.setDevukField(devukField);
    return f;
  }
}
