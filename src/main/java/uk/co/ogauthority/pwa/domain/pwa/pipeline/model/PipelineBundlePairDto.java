package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

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
