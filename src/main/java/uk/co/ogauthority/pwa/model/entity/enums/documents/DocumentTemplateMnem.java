package uk.co.ogauthority.pwa.model.entity.enums.documents;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;

public enum DocumentTemplateMnem {

  PWA_CONSENT_DOCUMENT(Set.of(
      DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT,
      DocumentSpec.DEPOSIT_CONSENT_DOCUMENT,
      DocumentSpec.VARIATION_CONSENT_DOCUMENT)
  );

  private Set<DocumentSpec> documentSpecs;

  DocumentTemplateMnem(Set<DocumentSpec> documentSpecs) {
    this.documentSpecs = documentSpecs;
  }

  public Set<DocumentSpec> getDocumentSpecs() {
    return documentSpecs;
  }

  public void setDocumentSpecs(
      Set<DocumentSpec> documentSpecs) {
    this.documentSpecs = documentSpecs;
  }

  public static Stream<DocumentTemplateMnem> stream() {
    return Arrays.stream(DocumentTemplateMnem.values());
  }

  public static DocumentTemplateMnem getMnemFromDocumentSpec(DocumentSpec documentSpec) {

    return stream()
        .filter(mnem -> mnem.getDocumentSpecs().contains(documentSpec))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(
            String.format("Couldn't find document template mnem for doc spec: %s", documentSpec.name())));

  }

}
