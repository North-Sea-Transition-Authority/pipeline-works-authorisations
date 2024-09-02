package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;

public class PwaConsentDto {

  private final Integer id;

  private final String reference;

  private final PwaConsentType consentType;

  private final Instant createdDate;

  private final Instant consentedDate;

  private final PwaDto pwa;

  public PwaConsentDto(Integer id, String reference, PwaConsentType consentType, Instant createdDate,
                       Instant consentedDate, Integer pwaId, String pwaReference, MasterPwaDetailStatus pwaStatus) {
    this.id = id;
    this.reference = reference;
    this.consentType = consentType;
    this.createdDate = createdDate;
    this.consentedDate = consentedDate;
    this.pwa = new PwaDto(pwaId, pwaReference, pwaStatus);
  }

  // No-args constructor required for Jackson mapping in controller test
  private PwaConsentDto() {
    id = null;
    reference = null;
    consentType = null;
    createdDate = null;
    consentedDate = null;
    pwa = null;
  }

  @JsonProperty
  public Integer getId() {
    return id;
  }

  @JsonProperty
  public String getReference() {
    return reference;
  }

  @JsonProperty
  public PwaConsentType getConsentType() {
    return consentType;
  }

  @JsonProperty
  public Instant getCreatedDate() {
    return createdDate;
  }

  @JsonProperty
  public Instant getConsentedDate() {
    return consentedDate;
  }

  @JsonProperty
  public PwaDto getPwa() {
    return pwa;
  }

}
