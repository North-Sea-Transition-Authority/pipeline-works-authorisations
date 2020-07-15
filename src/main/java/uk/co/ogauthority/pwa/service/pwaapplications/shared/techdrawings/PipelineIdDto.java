package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

public class PipelineIdDto {

  private final Integer pipelineId;
  private final Integer padPipelineId;

  public PipelineIdDto(Integer pipelineId, Integer padPipelineId) {
    this.pipelineId = pipelineId;
    this.padPipelineId = padPipelineId;
  }

  public Integer getPipelineId() {
    return pipelineId;
  }

  public Integer getPadPipelineId() {
    return padPipelineId;
  }
}
