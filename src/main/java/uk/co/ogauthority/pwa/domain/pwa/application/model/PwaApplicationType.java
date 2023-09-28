package uk.co.ogauthority.pwa.domain.pwa.application.model;

import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.features.application.creation.MedianLineImplication;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.model.enums.consents.ConsentIssueEmail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ParallelApplicationsWarning;

/**
 * Enumerates all types of application that can be submitted under the PWA process.
 */
// TODO: extract most of these enum fields from the domain and into the specific feature where they apply.
public enum PwaApplicationType {

  INITIAL(
      "New PWA",
      "initial",
      Period.ofMonths(4),
      Period.ofMonths(6),
      MedianLineImplication.TRUE,
      DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
      ConsentIssueEmail.NEW_PWA,
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
      ConsentIssueEmail.VARIATION,
      20,
      ParallelApplicationsWarning.SHOW_WARNING),

  CAT_2_VARIATION(
      "Cat. 2 Variation",
      "cat-2",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.TRUE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      ConsentIssueEmail.VARIATION,
      30,
      ParallelApplicationsWarning.SHOW_WARNING),

  HUOO_VARIATION(
      "HUOO Variation",
      "huoo",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.FALSE,
      DocumentSpec.HUOO_CONSENT_DOCUMENT,
      ConsentIssueEmail.HUOO,
      40,
      ParallelApplicationsWarning.SHOW_WARNING),

  DEPOSIT_CONSENT(
      "Deposit Consent",
      "dep",
      Period.ofWeeks(6),
      Period.ofWeeks(8),
      MedianLineImplication.FALSE,
      DocumentSpec.DEPOSIT_CONSENT_DOCUMENT,
      ConsentIssueEmail.DEPCON,
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
      ConsentIssueEmail.VARIATION,
      60,
      ParallelApplicationsWarning.SHOW_WARNING),

  DECOMMISSIONING(
      "Decommissioning",
      "decom",
      Period.ofMonths(6),
      Period.ofMonths(6),
      MedianLineImplication.TRUE,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT,
      ConsentIssueEmail.VARIATION,
      70,
      ParallelApplicationsWarning.SHOW_WARNING);

  private final String displayName;
  private final String urlPathString;
  private final Period minProcessingPeriod;
  private final Period maxProcessingPeriod;
  private final MedianLineImplication medianLineImplication;
  private final DocumentSpec consentDocumentSpec;
  private final ConsentIssueEmail consentIssueEmail;
  private final PwaConsentType pwaConsentType;
  private final int displayOrder;
  private final ParallelApplicationsWarning parallelApplicationsWarning;

  PwaApplicationType(String displayName,
                     String urlPathString,
                     Period minProcessingPeriod,
                     Period maxProcessingPeriod,
                     MedianLineImplication medianLineImplication,
                     DocumentSpec consentDocumentSpec,
                     ConsentIssueEmail consentIssueEmail,
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
    this.consentIssueEmail = consentIssueEmail;
    this.pwaConsentType = PwaConsentType.VARIATION;
  }

  PwaApplicationType(String displayName,
                     String urlPathString,
                     Period minProcessingPeriod,
                     Period maxProcessingPeriod,
                     MedianLineImplication medianLineImplication,
                     DocumentSpec consentDocumentSpec,
                     ConsentIssueEmail consentIssueEmail,
                     PwaConsentType pwaConsentType,
                     int displayOrder,
                     ParallelApplicationsWarning parallelApplicationsWarning) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
    this.minProcessingPeriod = minProcessingPeriod;
    this.maxProcessingPeriod = maxProcessingPeriod;
    this.medianLineImplication = medianLineImplication;
    this.consentDocumentSpec = consentDocumentSpec;
    this.consentIssueEmail = consentIssueEmail;
    this.pwaConsentType = pwaConsentType;
    this.displayOrder = displayOrder;
    this.parallelApplicationsWarning = parallelApplicationsWarning;
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

  public static List<PwaApplicationType> excluding(PwaApplicationType... excludedTypes) {
    var types = Arrays.stream(values())
        .collect(Collectors.toList());
    types.removeAll(Arrays.asList(excludedTypes));
    return types;
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

  public ConsentIssueEmail getConsentIssueEmail() {
    return consentIssueEmail;
  }

  public TemplateTextType getConsentEmailTemplateTextType() {
    return consentIssueEmail.getTemplateTextType();
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
