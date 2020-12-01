package uk.co.ogauthority.pwa.model.entity.enums.documents.generation;

import java.util.Map;

public enum DocumentSpec {

  INITIAL_APP_CONSENT_DOCUMENT(Map.of(
    DocumentSection.SCHEDULE_1, 10,
    DocumentSection.SCHEDULE_2, 20,
    DocumentSection.HUOO, 30,
    DocumentSection.TABLE_A, 40,
    DocumentSection.DEPOSITS, 50,
    DocumentSection.DEPOSIT_DRAWINGS, 60,
    DocumentSection.ADMIRALTY_CHART, 70
  )),

  DEPOSIT_CONSENT_DOCUMENT(Map.of(
      DocumentSection.DEPOSIT_DRAWINGS, 10
  ));

  private final Map<DocumentSection, Integer> documentSectionDisplayOrderMap;

  DocumentSpec(Map<DocumentSection, Integer> documentSectionDisplayOrderMap) {
    this.documentSectionDisplayOrderMap = documentSectionDisplayOrderMap;
  }

  public Map<DocumentSection, Integer> getDocumentSectionDisplayOrderMap() {
    return documentSectionDisplayOrderMap;
  }

}
