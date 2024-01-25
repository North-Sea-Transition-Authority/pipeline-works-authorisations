package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PipelineDto {
  private final Integer id;
  private final String number;
  private final PwaDto pwa;

  public PipelineDto(Integer id, String number, String pwaReference) {
    this.id = id;
    this.number = number;
    this.pwa = new PwaDto(pwaReference);
  }

  @JsonProperty
  public Integer getId() {
    return id;
  }

  @JsonProperty
  public String getNumber() {
    return number;
  }

  @JsonProperty
  public PwaDto getPwa() {
    return pwa;
  }
}
