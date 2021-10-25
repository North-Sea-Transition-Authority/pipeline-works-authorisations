package uk.co.ogauthority.pwa.repository.pipelines;

import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;

/**
 * Simply captures the total number of pipeline_detail records that exist for a pipeline.
 */
public final class CountPipelineDetailsForPipelineDto {

  private final PipelineId pipelineId;

  private final long countOfPipelineDetails;

  CountPipelineDetailsForPipelineDto(PipelineId pipelineId, long countOfPipelineDetails) {
    this.pipelineId = pipelineId;
    this.countOfPipelineDetails = countOfPipelineDetails;
  }

  // dto constructor
  public CountPipelineDetailsForPipelineDto(int pipelineId, long countOfPipelineDetails) {
    this(new PipelineId(pipelineId), countOfPipelineDetails);
  }

  public PipelineId getPipelineId() {
    return pipelineId;
  }

  public long getCountOfPipelineDetails() {
    return countOfPipelineDetails;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CountPipelineDetailsForPipelineDto that = (CountPipelineDetailsForPipelineDto) o;
    return countOfPipelineDetails == that.countOfPipelineDetails && Objects.equals(pipelineId, that.pipelineId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineId, countOfPipelineDetails);
  }
}
