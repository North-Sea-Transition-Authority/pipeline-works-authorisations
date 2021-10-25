package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

public class PipelineIdentifierTestUtil {

  public static PipelineSection createInclusivePipelineSection(int pipelineId, String fromLocation, String toLocation) {
    return createInclusivePipelineSection(
        pipelineId,
        fromLocation,
        toLocation,
        1
    );
  }

  public static PipelineSection createInclusivePipelineSection(int pipelineId, String fromLocation, String toLocation, Integer sectionNumber) {
    return PipelineSection.from(
        pipelineId,
        fromLocation,
        IdentLocationInclusionMode.INCLUSIVE,
        toLocation,
        IdentLocationInclusionMode.INCLUSIVE,
        sectionNumber
    );
  }
}
