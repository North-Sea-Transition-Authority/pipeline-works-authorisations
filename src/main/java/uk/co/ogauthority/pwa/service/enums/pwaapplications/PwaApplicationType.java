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
      MedianLineImplication.TRUE,
      DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
      TemplateTextType.INITIAL_CONSENT_EMAIL_COVER_LETTER,
      PwaConsentType.INITIAL_PWA,
      10,
      ParallelApplicationsWarning.NO_WARNING),

  CAT_1_VARIATION(
      "Cat. 1 Variation",
      "cat-1",
      Period.ofMonths(4),
      Period.ofMonths(6),
      MedianLineImplication.TRUE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      20,
      ParallelApplicationsWarning.SHOW_WARNING),

  CAT_2_VARIATION(
      "Cat. 2 Variation",
      "cat-2",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.TRUE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      30,
      ParallelApplicationsWarning.SHOW_WARNING),

  HUOO_VARIATION(
      "HUOO Variation",
      "huoo",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.FALSE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      40,

      ParallelApplicationsWarning.NO_WARNING),

  DEPOSIT_CONSENT(
      "Deposit Consent",
      "dep",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.FALSE,
      DocumentSpec.DEPOSIT_CONSENT_DOCUMENT,
      PwaConsentType.DEPOSIT_CONSENT,
      50,

      ParallelApplicationsWarning.NO_WARNING),

  OPTIONS_VARIATION(
      "Options Variation",
      "options",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.FALSE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      60,
      ParallelApplicationsWarning.SHOW_WARNING),

  DECOMMISSIONING(
      "Decommissioning",
      "decom",
      Period.ofMonths(6),
      Period.ofMonths(6),
      MedianLineImplication.TRUE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      70,
      ParallelApplicationsWarning.SHOW_WARNING);

  private final String displayName;
  private final String urlPathString;

  private final Period minProcessingPeriod;
  private final Period maxProcessingPeriod;
  private final MedianLineImplication medianLineImplication;

  private final DocumentSpec consentDocumentSpec;
  private final TemplateTextType consentEmailTemplateTextType;

  private final PwaConsentType pwaConsentType;
  private final int displayOrder;

  private final ParallelApplicationsWarning parallelApplicationsWarning;

  PwaApplicationType(String displayName,
                     String urlPathString,
                     Period minProcessingPeriod,
                     Period maxProcessingPeriod,
                     MedianLineImplication medianLineImplication,
                     DocumentSpec consentDocumentSpec,
                     int displayOrder,
                     ParallelApplicationsWarning parallelApplicationsWarning) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
    this.minProcessingPeriod = minProcessingPeriod;
    this.maxProcessingPeriod = maxProcessingPeriod;
    this.medianLineImplication = medianLineImplication;
    this.consentDocumentSpec = consentDocumentSpec;
    this.displayOrder = displayOrder;
    this.parallelApplicationsWarning = parallelApplicationsWarning;
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
                     int displayOrder,
                     ParallelApplicationsWarning parallelApplicationsWarning) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
    this.minProcessingPeriod = minProcessingPeriod;
    this.maxProcessingPeriod = maxProcessingPeriod;
    this.medianLineImplication = medianLineImplication;
    this.consentDocumentSpec = consentDocumentSpec;
    this.consentEmailTemplateTextType = consentEmailTemplateTextType;
    this.pwaConsentType = pwaConsentType;
    this.displayOrder = displayOrder;
    this.parallelApplicationsWarning = parallelApplicationsWarning;
  }

  PwaApplicationType(String displayName,
                     String urlPathString,
                     Period minProcessingPeriod,
                     Period maxProcessingPeriod,
                     MedianLineImplication medianLineImplication,
                     DocumentSpec consentDocumentSpec,
                     PwaConsentType pwaConsentType,
                     int displayOrder,
                     ParallelApplicationsWarning parallelApplicationsWarning) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
    this.minProcessingPeriod = minProcessingPeriod;
    this.maxProcessingPeriod = maxProcessingPeriod;
    this.medianLineImplication = medianLineImplication;
    this.consentDocumentSpec = consentDocumentSpec;
    this.displayOrder = displayOrder;
    this.parallelApplicationsWarning = parallelApplicationsWarning;
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

  public ParallelApplicationsWarning getParallelApplicationsWarning() {
    return parallelApplicationsWarning;
  }

  public static Stream<PwaApplicationType> stream() {
    return Stream.of(PwaApplicationType.values());
  }
}
