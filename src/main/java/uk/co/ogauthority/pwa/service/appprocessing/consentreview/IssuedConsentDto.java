package uk.co.ogauthority.pwa.service.appprocessing.consentreview;

import java.util.Objects;

public final class IssuedConsentDto {
  private final String consentReference;

  IssuedConsentDto(String consentReference) {
    this.consentReference = consentReference;
  }

  public String getConsentReference() {
    return consentReference;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IssuedConsentDto that = (IssuedConsentDto) o;
    return Objects.equals(consentReference, that.consentReference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentReference);
  }
}
