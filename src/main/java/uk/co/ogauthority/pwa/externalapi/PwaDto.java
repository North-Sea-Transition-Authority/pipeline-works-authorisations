package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PwaDto {

  private final Integer id;
  private final String reference;

  public PwaDto(Integer id, String reference) {
    this.id = id;
    this.reference = reference;
  }

  // No-args constructor required for Jackson mapping in controller test
  private PwaDto() {
    id = null;
    reference = null;
  }

  @JsonProperty
  public String getReference() {
    return reference;
  }

  @JsonProperty
  public Integer getId() {
    return id;
  }
}
