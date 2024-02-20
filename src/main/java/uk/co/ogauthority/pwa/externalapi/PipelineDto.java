package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PipelineDto {
  private final Integer id;
  private final String pipelineNumber;
  private final PwaDto pwa;

  public PipelineDto(Integer id, String pipelineNumber, Integer pwaId, String pwaReference) {
    this.id = id;
    this.pipelineNumber = pipelineNumber;
    this.pwa = new PwaDto(pwaId, pwaReference);
  }

  @JsonProperty
  public Integer getId() {
    return id;
  }

  @JsonProperty
  public String getPipelineNumber() {
    return pipelineNumber;
  }

  @JsonProperty
  public PwaDto getPwa() {
    return pwa;
  }
}
