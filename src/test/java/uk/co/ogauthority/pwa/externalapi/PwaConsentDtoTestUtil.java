package uk.co.ogauthority.pwa.externalapi;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;

public class PwaConsentDtoTestUtil {

  private PwaConsentDtoTestUtil() {
    throw new IllegalStateException("Cannot instantiate static util class");
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id;
    private String reference;
    private PwaConsentType consentType;
    private Instant createdDate;
    private Instant consentedDate;
    private Integer pwaId;
    private String pwaReference;
    private MasterPwaDetailStatus pwaStatus;

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withReference(String reference) {
      this.reference = reference;
      return this;
    }

    public Builder withConsentType(PwaConsentType consentType) {
      this.consentType = consentType;
      return this;
    }

    public Builder withCreatedDate(Instant createdDate) {
      this.createdDate = createdDate;
      return this;
    }

    public Builder withConsentedDate(Instant consentedDate) {
      this.consentedDate = consentedDate;
      return this;
    }

    public Builder withPwaId(Integer pwaId) {
      this.pwaId = pwaId;
      return this;
    }

    public Builder withPwaReference(String pwaReference) {
      this.pwaReference = pwaReference;
      return this;
    }

    public Builder withPwaStatus(MasterPwaDetailStatus pwaStatus) {
      this.pwaStatus = pwaStatus;
      return this;
    }

    public PwaConsentDto build() {
      return new PwaConsentDto(
          id,
          reference,
          consentType,
          createdDate,
          consentedDate,
          pwaId,
          pwaReference,
          pwaStatus
      );
    }
  }
}
