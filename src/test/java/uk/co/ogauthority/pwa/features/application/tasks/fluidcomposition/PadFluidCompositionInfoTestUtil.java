package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadFluidCompositionInfoTestUtil {

  private PadFluidCompositionInfoTestUtil() {
    // no instantiation
  }

  public static PadFluidCompositionInfo createTraceFluid(PwaApplicationDetail pwaApplicationDetail, Chemical chemical){
    var fluid = new PadFluidCompositionInfo(pwaApplicationDetail, chemical);
    fluid.setFluidCompositionOption(FluidCompositionOption.TRACE);

    ObjectTestUtils.assertAllFieldsNotNull(
        fluid,
        PadFluidCompositionInfo.class,
        Set.of(PadFluidCompositionInfo_.ID, PadFluidCompositionInfo_.MOLE_VALUE));

    return fluid;
  }

  public static PadFluidCompositionInfo createNotPresentFluid(PwaApplicationDetail pwaApplicationDetail, Chemical chemical){
    var fluid = new PadFluidCompositionInfo(pwaApplicationDetail, chemical);
    fluid.setFluidCompositionOption(FluidCompositionOption.NONE);

    ObjectTestUtils.assertAllFieldsNotNull(
        fluid,
        PadFluidCompositionInfo.class,
        Set.of(PadFluidCompositionInfo_.ID, PadFluidCompositionInfo_.MOLE_VALUE));

    return fluid;
  }

  public static PadFluidCompositionInfo createSignificantFluid(PwaApplicationDetail pwaApplicationDetail, Chemical chemical){
    var fluid = new PadFluidCompositionInfo(pwaApplicationDetail, chemical);
    fluid.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    fluid.setMoleValue(BigDecimal.valueOf(RandomUtils.nextDouble(0, 100)).setScale(2, RoundingMode.HALF_UP));

    ObjectTestUtils.assertAllFieldsNotNull(
        fluid,
        PadFluidCompositionInfo.class,
        Set.of(PadFluidCompositionInfo_.ID));

    return fluid;
  }
}
