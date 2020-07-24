package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

public class PipelineDetailIdentCountDto {

  private Integer pipelineId;
  private Long identCount;

  public PipelineDetailIdentCountDto(Integer pipelineId, Long identCount) {
    this.pipelineId = pipelineId;
    this.identCount = identCount;
  }

  public Integer getPipelineId() {
    return pipelineId;
  }

  public Long getIdentCount() {
    return identCount;
  }
}
