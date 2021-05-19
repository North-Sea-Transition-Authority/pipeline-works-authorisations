package uk.co.ogauthority.pwa.model.entity.enums.documents.generation;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DocumentSpec {

  INITIAL_APP_CONSENT_DOCUMENT(Map.of(
    DocumentSection.INITIAL_INTRO, 10,
    DocumentSection.INITIAL_TERMS_AND_CONDITIONS, 20,
    DocumentSection.HUOO, 30,
    DocumentSection.SCHEDULE_2, 40,
    DocumentSection.TABLE_A, 50,
    DocumentSection.DEPOSITS, 60,
    DocumentSection.DEPOSIT_DRAWINGS, 70,
    DocumentSection.ADMIRALTY_CHART, 80
  ), "New PWA consent document"),

  DEPOSIT_CONSENT_DOCUMENT(Map.of(
      DocumentSection.DEPCON_INTRO, 10,
      DocumentSection.DEPOSITS, 20,
      DocumentSection.DEPOSIT_DRAWINGS, 30
  ), "Deposit consent document"),

  VARIATION_CONSENT_DOCUMENT(Map.of(
      DocumentSection.VARIATION_INTRO, 10,
      DocumentSection.HUOO, 20,
      DocumentSection.TABLE_A, 30,
      DocumentSection.DEPOSITS, 40,
      DocumentSection.DEPOSIT_DRAWINGS, 50,
      DocumentSection.ADMIRALTY_CHART, 60
  ), "Variation consent document");

  private final Map<DocumentSection, Integer> documentSectionDisplayOrderMap;
  private final String displayName;

  DocumentSpec(
      Map<DocumentSection, Integer> documentSectionDisplayOrderMap, String displayName) {
    this.documentSectionDisplayOrderMap = documentSectionDisplayOrderMap;
    this.displayName = displayName;
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

  public static Stream<DocumentSpec> stream() {
    return Arrays.stream(DocumentSpec.values());
  }

}
