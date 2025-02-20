package uk.co.ogauthority.pwa.model.entity.enums.documents.generation;

public enum SectionType {

  /**
   * The section relies on user-edited clauses, a generic display can be used for all such sections.
   */
  CLAUSE_LIST,

  /**
   * The section relies on developer-made custom display using service data.
   */
  CUSTOM,

  /**
   * The section represents the opening page/paragraph of a document.
   */
  OPENING_PARAGRAPH,

  DIGITAL_SIGNATURE,

}
