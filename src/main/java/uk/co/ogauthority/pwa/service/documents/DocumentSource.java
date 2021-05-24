package uk.co.ogauthority.pwa.service.documents;

import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;

/**
 * A class that implements this can act as the key or source for a generated document, for example a PWA application.
 */
public interface DocumentSource {

  Object getSource();

  DocumentSpec getDocumentSpec();

  /**
   * Whether or not manual merge data can be saved in clauses by users.
   */
  default boolean manualMergeAllowed() {
    return false;
  }

}
