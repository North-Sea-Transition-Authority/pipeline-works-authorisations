package uk.co.ogauthority.pwa.features.termsandconditions.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.Immutable;

@Entity
@Immutable
@Table(name = "vw_pwa_terms_and_conditions")
public class TermsAndConditionsPwaView {

  @Id
  private int pwaId;

  private String consentReference;

  public TermsAndConditionsPwaView() {}

  public TermsAndConditionsPwaView(int pwaId, String consentReference) {
    this.pwaId = pwaId;
    this.consentReference = consentReference;
  }

  public int getPwaId() {
    return pwaId;
  }

  public String getConsentReference() {
    return consentReference;
  }
}