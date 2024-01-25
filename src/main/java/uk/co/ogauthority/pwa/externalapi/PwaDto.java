package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PwaDto {

  private final String reference;

  public PwaDto(String reference) {
    this.reference = reference;
  }

  @JsonProperty
  public String getReference() {
    return reference;
  }
}
