package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.time.Period;
import java.util.Optional;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;

/**
 * Enumerates all types of application that can be submitted under the PWA process.
 */
public enum PwaApplicationType {

  INITIAL(
      "New PWA",
      "initial",
      Period.ofMonths(4),
      Period.ofMonths(6),
      MedianLineImplication.FALSE,
      DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
      TemplateTextType.INITIAL_CONSENT_EMAIL_COVER_LETTER,
      PwaConsentType.INITIAL_PWA,
      10),

  DEPOSIT_CONSENT(
      "Deposit Consent",
      "dep",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.FALSE,
      DocumentSpec.DEPOSIT_CONSENT_DOCUMENT,
      PwaConsentType.DEPOSIT_CONSENT,
      20),

  CAT_1_VARIATION(
      "Cat. 1 Variation",
      "cat-1",
      Period.ofMonths(4),
      Period.ofMonths(6),
      MedianLineImplication.TRUE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      30),

  CAT_2_VARIATION(
      "Cat. 2 Variation",
      "cat-2",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.TRUE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      40),

  HUOO_VARIATION(
      "HUOO Variation",
      "huoo",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.FALSE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      50),

  OPTIONS_VARIATION(
      "Options Variation",
      "options",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.FALSE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      60),

  DECOMMISSIONING(
      "Decommissioning",
      "decom",
      Period.ofMonths(6),
      Period.ofMonths(6),
      MedianLineImplication.TRUE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      70);

  private final String displayName;
  private final String urlPathString;

  private final Period minProcessingPeriod;
  private final Period maxProcessingPeriod;
  private final MedianLineImplication medianLineImplication;

  private final DocumentSpec consentDocumentSpec;
  private final TemplateTextType consentEmailTemplateTextType;

  private final PwaConsentType pwaConsentType;
  private final int displayOrder;

  PwaApplicationType(String displayName,
                     String urlPathString,
                     Period minProcessingPeriod,
                     Period maxProcessingPeriod,
                     MedianLineImplication medianLineImplication,
                     DocumentSpec consentDocumentSpec,
                     int displayOrder) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
    this.minProcessingPeriod = minProcessingPeriod;
    this.maxProcessingPeriod = maxProcessingPeriod;
    this.medianLineImplication = medianLineImplication;
    this.consentDocumentSpec = consentDocumentSpec;
    this.displayOrder = displayOrder;
    this.consentEmailTemplateTextType = TemplateTextType.VARIATION_CONSENT_EMAIL_COVER_LETTER;
    this.pwaConsentType = PwaConsentType.VARIATION;
  }

  PwaApplicationType(String displayName,
                     String urlPathString,
                     Period minProcessingPeriod,
                     Period maxProcessingPeriod,
                     MedianLineImplication medianLineImplication,
                     DocumentSpec consentDocumentSpec,
                     TemplateTextType consentEmailTemplateTextType,
                     PwaConsentType pwaConsentType,
                     int displayOrder) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
    this.minProcessingPeriod = minProcessingPeriod;
    this.maxProcessingPeriod = maxProcessingPeriod;
    this.medianLineImplication = medianLineImplication;
    this.consentDocumentSpec = consentDocumentSpec;
    this.consentEmailTemplateTextType = consentEmailTemplateTextType;
    this.pwaConsentType = pwaConsentType;
    this.displayOrder = displayOrder;
  }

  PwaApplicationType(String displayName,
                     String urlPathString,
                     Period minProcessingPeriod,
                     Period maxProcessingPeriod,
                     MedianLineImplication medianLineImplication,
                     DocumentSpec consentDocumentSpec,
                     PwaConsentType pwaConsentType,
                     int displayOrder) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
    this.minProcessingPeriod = minProcessingPeriod;
    this.maxProcessingPeriod = maxProcessingPeriod;
    this.medianLineImplication = medianLineImplication;
    this.consentDocumentSpec = consentDocumentSpec;
    this.displayOrder = displayOrder;
    this.consentEmailTemplateTextType = TemplateTextType.VARIATION_CONSENT_EMAIL_COVER_LETTER;
    this.pwaConsentType = pwaConsentType;
  }

  public static Optional<PwaApplicationType> getFromUrlPathString(String urlPathValue) {
    for (PwaApplicationType type : PwaApplicationType.values()) {
      if (type.getUrlPathString().equals(urlPathValue)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }

  public static PwaApplicationType resolveFromDisplayText(String applicationTypeDisplay) {
    return PwaApplicationType.stream()
        .filter(type -> type.getDisplayName().equals(applicationTypeDisplay))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(
            String.format("Couldn't find PwaApplicationType value for display string: %s", applicationTypeDisplay)));
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUrlPathString() {
    return urlPathString;
  }

  public Period getMinProcessingPeriod() {
    return minProcessingPeriod;
  }

  public Period getMaxProcessingPeriod() {
    return maxProcessingPeriod;
  }

  public MedianLineImplication getMedianLineImplication() {
    return medianLineImplication;
  }

  public DocumentSpec getConsentDocumentSpec() {
    return consentDocumentSpec;
  }

  public TemplateTextType getConsentEmailTemplateTextType() {
    return consentEmailTemplateTextType;
  }

  public PwaConsentType getPwaConsentType() {
    return pwaConsentType;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<PwaApplicationType> stream() {
    return Stream.of(PwaApplicationType.values());
  }
}
