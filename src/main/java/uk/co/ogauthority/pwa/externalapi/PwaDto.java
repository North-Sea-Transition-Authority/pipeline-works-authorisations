package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.core.lang.NonNull;

public class PwaDto implements Comparable<PwaDto> {

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

  @Override
  public int compareTo(@NonNull PwaDto pwaDtoToCompare) {
    return new PwaReferenceComparator().compare(this, pwaDtoToCompare);
  }
}
