package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

public enum PickablePipelineType {

  CONSENTED, APPLICATION, UNKNOWN;

  public String createIdString(int id) {
    return String.format("%s++%s", id, this.name());
  }

  public static Integer getIntegerIdFromString(String idString) {
    var type = getTypeIdFromString(idString);
    if (UNKNOWN.equals(type)) {
      return null;
    }

    return Integer.valueOf(idString.replace("++" + type, ""));

  }

  public static String getPickableString(PipelineDetail pipelineDetail) {
    return CONSENTED.createIdString(pipelineDetail.getPipelineId());

  }

  public static String getPickableString(PadPipelineSummaryDto padPipelineSummaryDto) {
    return APPLICATION.createIdString(padPipelineSummaryDto.getPadPipelineId());
  }

  public static PickablePipelineType getTypeIdFromString(String idString) {
    if (idString.endsWith("++" + CONSENTED)) {
      return CONSENTED;
    } else if (idString.endsWith("++" + APPLICATION)) {
      return APPLICATION;
    }
    return UNKNOWN;
  }

  public Stream<PickablePipelineType> stream() {
    return Arrays.stream(PickablePipelineType.values());
  }
}