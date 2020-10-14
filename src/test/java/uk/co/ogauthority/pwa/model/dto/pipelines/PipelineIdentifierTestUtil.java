package uk.co.ogauthority.pwa.model.dto.pipelines;

public class PipelineIdentifierTestUtil {

  public static PipelineSection createInclusivePipelineSection(int pipelineId, String fromLocation, String toLocation) {
    return PipelineSection.from(
        pipelineId,
        fromLocation,
        IdentLocationInclusionMode.INCLUSIVE,
        toLocation,
        IdentLocationInclusionMode.INCLUSIVE
        );
  }
}
