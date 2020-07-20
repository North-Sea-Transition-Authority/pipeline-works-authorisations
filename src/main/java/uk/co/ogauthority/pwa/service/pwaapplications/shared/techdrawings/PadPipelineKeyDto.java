package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

/**
 * Provides access to both the PadPipelineId and the PipelineId.
 * Used to clarify which ID is being used to improve readability.
 */
public class PadPipelineKeyDto {

  private final Integer pipelineId;
  private final Integer padPipelineId;

  public PadPipelineKeyDto(Integer pipelineId, Integer padPipelineId) {
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
