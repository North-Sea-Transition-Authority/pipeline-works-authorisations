package uk.co.ogauthority.pwa.model.dto.pipelines;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;

public class PadPipelineSummaryDtoTestUtils {

  public PadPipelineSummaryDtoTestUtils() {
    throw new AssertionError();
  }

  public static PadPipelineSummaryDto generateFrom(PadPipeline padPipeline) {

    return new PadPipelineSummaryDto(
        padPipeline.getId(),
        padPipeline.getPipeline().getId(),
        PipelineType.PRODUCTION_FLOWLINE,
        padPipeline.getPipelineRef(),
        padPipeline.getTemporaryRef(),
        BigDecimal.TEN,
        "OIL",
        "PRODUCTS",
        1L,
        "STRUCT_A",
        45,
        45,
        BigDecimal.valueOf(45),
        LatitudeDirection.NORTH,
        1,
        1,
        BigDecimal.ONE,
        LongitudeDirection.EAST,
        "STRUCT_B",
        46,
        46,
        BigDecimal.valueOf(46),
        LatitudeDirection.NORTH,
        2,
        2,
        BigDecimal.valueOf(2),
        LongitudeDirection.EAST,
        padPipeline.getMaxExternalDiameter(),
        padPipeline.getPipelineInBundle(),
        padPipeline.getBundleName(),
        padPipeline.getPipelineFlexibility(),
        padPipeline.getPipelineMaterial(),
        padPipeline.getOtherPipelineMaterialUsed(),
        padPipeline.getTrenchedBuriedBackfilled(),
        padPipeline.getTrenchingMethodsDescription(),
        padPipeline.getPipelineStatus(),
        padPipeline.getPipelineStatusReason(),
        padPipeline.getAlreadyExistsOnSeabed(),
        padPipeline.getPipelineInUse(),
        padPipeline.getFootnote());

  }

}
