package uk.co.ogauthority.pwa.model.entity.enums.documents.generation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
  )),

  DEPOSIT_CONSENT_DOCUMENT(Map.of(
      DocumentSection.DEPCON_INTRO, 10,
      DocumentSection.DEPOSITS, 20,
      DocumentSection.DEPOSIT_DRAWINGS, 30
  )),

  VARIATION_CONSENT_DOCUMENT(Map.of(
      DocumentSection.VARIATION_INTRO, 10,
      DocumentSection.HUOO, 20,
      DocumentSection.TABLE_A, 30,
      DocumentSection.DEPOSITS, 40,
      DocumentSection.DEPOSIT_DRAWINGS, 50,
      DocumentSection.ADMIRALTY_CHART, 60
  ));

  private final Map<DocumentSection, Integer> documentSectionDisplayOrderMap;

  DocumentSpec(Map<DocumentSection, Integer> documentSectionDisplayOrderMap) {
    this.documentSectionDisplayOrderMap = documentSectionDisplayOrderMap;
  }

  public Map<DocumentSection, Integer> getDocumentSectionDisplayOrderMap() {
    return documentSectionDisplayOrderMap;
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

}
