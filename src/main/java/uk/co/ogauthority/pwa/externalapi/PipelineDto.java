package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PipelineDto {
  private final Integer pipelineId;
  private final String pipelineNumber;
  private final String pwaReference;

  public PipelineDto(Integer pipelineId, String pipelineNumber, String pwaReference) {
    this.pipelineId = pipelineId;
    this.pipelineNumber = pipelineNumber;
    this.pwaReference = pwaReference;
  }

  @JsonProperty
  public Integer getPipelineId() {
    return pipelineId;
  }

  @JsonProperty
  public String getPipelineNumber() {
    return pipelineNumber;
  }

  @JsonProperty
  public String getPwaReference() {
    return pwaReference;
  }
}
