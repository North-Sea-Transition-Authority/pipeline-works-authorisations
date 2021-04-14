package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.time.Instant;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.DateUtils;

public class PwaConsentApplicationDto {


  private final Integer consentId;
  private final Instant consentInstant;
  private final String consentReference;
  private final Integer pwaApplicationId;
  private final PwaApplicationType applicationType;
  private final String appReference;


  public PwaConsentApplicationDto(Integer consentId,
                                  Instant consentInstant,
                                  String consentReference,
                                  Integer pwaApplicationId,
                                  PwaApplicationType pwaApplicationType,
                                  String appReference) {
    this.consentId = consentId;
    this.consentInstant = consentInstant;
    this.consentReference = consentReference;
    this.pwaApplicationId = pwaApplicationId;
    this.applicationType = pwaApplicationType;
    this.appReference = appReference;
  }

  public Integer getConsentId() {
    return consentId;
  }

  public Instant getConsentInstant() {
    return consentInstant;
  }

  public String getConsentDateDisplay() {
    return DateUtils.formatDate(consentInstant);
  }

  public String getConsentReference() {
    return consentReference;
  }

  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public PwaApplicationType getApplicationType() {
    return applicationType;
  }

  public String getAppReference() {
    return appReference;
  }
}
