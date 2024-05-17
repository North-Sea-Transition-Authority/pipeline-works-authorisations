package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.core.lang.NonNull;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;

public class PwaDto implements Comparable<PwaDto> {

  private final Integer id;
  private final String reference;
  private final MasterPwaDetailStatus status;

  public PwaDto(Integer id, String reference, MasterPwaDetailStatus status) {
    this.id = id;
    this.reference = reference;
    this.status = status;
  }

  // No-args constructor required for Jackson mapping in controller test
  private PwaDto() {
    id = null;
    reference = null;
    status = null;
  }

  @JsonProperty
  public String getReference() {
    return reference;
  }

  @JsonProperty
  public Integer getId() {
    return id;
  }

  @JsonProperty
  public MasterPwaDetailStatus getStatus() {
    return status;
  }

  @Override
  public int compareTo(@NonNull PwaDto pwaDtoToCompare) {
    return new PwaReferenceComparator().compare(this, pwaDtoToCompare);
  }
}
