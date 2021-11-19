package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

import java.time.Instant;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

/**
 * Stores information about a consent that has been consented in the time period between the
 * current application's creation and the current moment.
 */
public final class ParallelConsentView {

  private final int pwaConsentId;
  private final String consentReference;
  private final Integer pwaApplicationId;
  private final PwaApplicationType pwaApplicationType;
  private final String applicationReference;
  private final Instant consentInstant;
  private final String formattedConsentDate;

  ParallelConsentView(int pwaConsentId,
                      String consentReference,
                      Integer pwaApplicationId,
                      PwaApplicationType pwaApplicationType,
                      String applicationReference,
                      Instant consentInstant,
                      String formattedConsentDate) {
    this.pwaConsentId = pwaConsentId;
    this.consentReference = consentReference;
    this.pwaApplicationId = pwaApplicationId;
    this.pwaApplicationType = pwaApplicationType;
    this.applicationReference = applicationReference;
    this.consentInstant = consentInstant;
    this.formattedConsentDate = formattedConsentDate;
  }

  public int getPwaConsentId() {
    return pwaConsentId;
  }

  public String getConsentReference() {
    return consentReference;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public Instant getConsentInstant() {
    return consentInstant;
  }

  public String getFormattedConsentDate() {
    return formattedConsentDate;
  }

  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public PwaApplicationType getPwaApplicationType() {
    return pwaApplicationType;
  }
}
