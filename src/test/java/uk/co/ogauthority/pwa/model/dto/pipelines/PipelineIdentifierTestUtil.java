package uk.co.ogauthority.pwa.model.dto.pipelines;

public class PipelineIdentifierTestUtil {

  public static PipelineSegment createInclusivePipelineSegment(int pipelineId, String fromLocation, String toLocation) {
    return PipelineSegment.from(
        pipelineId,
        fromLocation,
        IdentLocationInclusionMode.INCLUSIVE,
        toLocation,
        IdentLocationInclusionMode.INCLUSIVE
        );
  }
}
