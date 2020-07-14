package uk.co.ogauthority.pwa.repository.pipelines;

public class PipelineBundlePairDto {

  private final Integer pipelineId;
  private final String bundleName;

  public PipelineBundlePairDto(Integer pipelineId, String bundleName) {
    this.pipelineId = pipelineId;
    this.bundleName = bundleName;
  }

  public Integer getPipelineId() {
    return pipelineId;
  }

  public String getBundleName() {
    return bundleName;
  }
}
