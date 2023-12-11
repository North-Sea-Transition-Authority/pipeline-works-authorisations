package uk.co.ogauthority.pwa.model.entity.enums.documents.generation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;

public enum DocumentSpec {

  INITIAL_PETROLEUM_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.INITIAL_INTRO, 10,
          DocumentSection.INITIAL_TERMS_AND_CONDITIONS, 20,
          DocumentSection.HUOO, 30,
          DocumentSection.SCHEDULE_2, 40,
          DocumentSection.TABLE_A, 50,
          DocumentSection.DEPOSITS, 60,
          DocumentSection.DEPOSIT_DRAWINGS, 70,
          DocumentSection.ADMIRALTY_CHART, 80),
      "New PWA consent document - Petroleum",
      List.of(
          PwaApplicationType.INITIAL)),

  DEPOSIT_PETROLEUM_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.DEPCON_INTRO, 10,
          DocumentSection.DEPOSITS, 20,
          DocumentSection.DEPOSIT_DRAWINGS, 30),
      "Deposit consent document - Petroleum",
      List.of(
          PwaApplicationType.DEPOSIT_CONSENT)),

  VARIATION_PETROLEUM_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.VARIATION_INTRO, 10,
          DocumentSection.HUOO, 20,
          DocumentSection.TABLE_A, 30,
          DocumentSection.DEPOSITS, 40,
          DocumentSection.DEPOSIT_DRAWINGS, 50,
          DocumentSection.ADMIRALTY_CHART, 60),
      "Variation consent document - Petroleum",
      List.of(
          PwaApplicationType.CAT_1_VARIATION,
          PwaApplicationType.CAT_2_VARIATION,
          PwaApplicationType.OPTIONS_VARIATION,
          PwaApplicationType.DECOMMISSIONING
      )),

  HUOO_PETROLEUM_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.HUOO_INTRO, 10,
          DocumentSection.HUOO, 20),
      "HUOO consent document - Petroleum",
      List.of(
          PwaApplicationType.HUOO_VARIATION
      )),

  INITIAL_HYDROGEN_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.INITIAL_INTRO, 10,
          DocumentSection.INITIAL_TERMS_AND_CONDITIONS, 20,
          DocumentSection.HUOO, 30,
          DocumentSection.SCHEDULE_2, 40,
          DocumentSection.TABLE_A, 50,
          DocumentSection.DEPOSITS, 60,
          DocumentSection.DEPOSIT_DRAWINGS, 70,
          DocumentSection.ADMIRALTY_CHART, 80),
      "New PWA consent document - Hydrogen",
      List.of(
          PwaApplicationType.INITIAL
      )),

  DEPOSIT_HYDROGEN_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.DEPCON_INTRO, 10,
          DocumentSection.DEPOSITS, 20,
          DocumentSection.DEPOSIT_DRAWINGS, 30),
      "Deposit consent document - Hydrogen",
      List.of(
          PwaApplicationType.DEPOSIT_CONSENT
      )),
  VARIATION_HYDROGEN_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.VARIATION_INTRO, 10,
          DocumentSection.HUOO, 20,
          DocumentSection.TABLE_A, 30,
          DocumentSection.DEPOSITS, 40,
          DocumentSection.DEPOSIT_DRAWINGS, 50,
          DocumentSection.ADMIRALTY_CHART, 60),
      "Variation consent document - Hydrogen",
      List.of(
          PwaApplicationType.CAT_1_VARIATION,
          PwaApplicationType.CAT_2_VARIATION,
          PwaApplicationType.OPTIONS_VARIATION,
          PwaApplicationType.DECOMMISSIONING
      )),

  HUOO_HYDROGEN_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.HUOO_INTRO, 10,
          DocumentSection.HUOO, 20),
      "HUOO consent document - Hydrogen",
      List.of(
          PwaApplicationType.HUOO_VARIATION
      )),
  INITIAL_CCUS_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.INITIAL_INTRO, 10,
          DocumentSection.INITIAL_TERMS_AND_CONDITIONS, 20,
          DocumentSection.HUOO, 30,
          DocumentSection.SCHEDULE_2, 40,
          DocumentSection.TABLE_A, 50,
          DocumentSection.DEPOSITS, 60,
          DocumentSection.DEPOSIT_DRAWINGS, 70,
          DocumentSection.ADMIRALTY_CHART, 80),
      "New PWA consent document - CCUS",
      List.of(
          PwaApplicationType.INITIAL
      )),

  DEPOSIT_CCUS_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.DEPCON_INTRO, 10,
          DocumentSection.DEPOSITS, 20,
          DocumentSection.DEPOSIT_DRAWINGS, 30),
      "Deposit consent document - CCUS",
      List.of(
          PwaApplicationType.INITIAL
      )),

  VARIATION_CCUS_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.VARIATION_INTRO, 10,
          DocumentSection.HUOO, 20,
          DocumentSection.TABLE_A, 30,
          DocumentSection.DEPOSITS, 40,
          DocumentSection.DEPOSIT_DRAWINGS, 50,
          DocumentSection.ADMIRALTY_CHART, 60),
      "Variation consent document - CCUS",
      List.of(
          PwaApplicationType.CAT_1_VARIATION,
          PwaApplicationType.CAT_2_VARIATION,
          PwaApplicationType.OPTIONS_VARIATION,
          PwaApplicationType.DECOMMISSIONING
      )),

  HUOO_CCUS_CONSENT_DOCUMENT(
      Map.of(
          DocumentSection.HUOO_INTRO, 10,
          DocumentSection.HUOO, 20),
      "HUOO consent document - CCUS",
      List.of(
          PwaApplicationType.HUOO_VARIATION
      ));

  private final Map<DocumentSection, Integer> documentSectionDisplayOrderMap;
  private final String displayName;

  private final List<PwaApplicationType> applicationType;

  DocumentSpec(Map<DocumentSection, Integer> documentSectionDisplayOrderMap,
               String displayName,
               List<PwaApplicationType> applicationType) {
    this.documentSectionDisplayOrderMap = documentSectionDisplayOrderMap;
    this.displayName = displayName;
    this.applicationType = applicationType;
  }

  public Map<DocumentSection, Integer> getDocumentSectionDisplayOrderMap() {
    return documentSectionDisplayOrderMap;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Set<String> getSectionNames() {
    return documentSectionDisplayOrderMap.keySet().stream()
        .map(Enum::name)
        .collect(Collectors.toSet());
  }

  public int getDisplayOrder(DocumentSection section) {
    return this.documentSectionDisplayOrderMap.entrySet().stream()
        .filter(e -> e.getKey() == section)
        .findFirst()
        .map(Map.Entry::getValue)
        .orElseThrow(() -> new RuntimeException(String.format(
            "Couldn't get docspec display order for section: [%s] and docspec: [%s]",
            section.name(),
            this.name())));
  }

  public List<PwaApplicationType> getApplicationType() {
    return applicationType;
  }

  public static DocumentSpec getSpecForApplication(PwaApplication application) {
    return getSpecForApplication(application.getApplicationType(), application.getResourceType());
  }

  public static DocumentSpec getSpecForApplication(PwaApplicationType applicationType, PwaResourceType resourceType) {
    var docMnem = DocumentTemplateMnem.getMnemFromResourceType(resourceType);
    return docMnem.getDocumentSpecs()
        .stream()
        .filter(spec -> spec.getApplicationType().contains(applicationType))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException(
            String.format(
                "Could not find DocumentSpec for %s : %s",
                applicationType,
                resourceType)));
  }

  public static Stream<DocumentSpec> stream() {
    return Arrays.stream(DocumentSpec.values());
  }

}
