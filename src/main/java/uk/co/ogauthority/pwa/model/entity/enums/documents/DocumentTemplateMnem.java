package uk.co.ogauthority.pwa.model.entity.enums.documents;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;

public enum DocumentTemplateMnem {

  PETROLEUM_CONSENT_DOCUMENT(Set.of(
      DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT,
      DocumentSpec.DEPOSIT_PETROLEUM_CONSENT_DOCUMENT,
      DocumentSpec.VARIATION_PETROLEUM_CONSENT_DOCUMENT,
      DocumentSpec.HUOO_PETROLEUM_CONSENT_DOCUMENT)
  ),
  HYDROGEN_CONSENT_DOCUMENT(Set.of(
      DocumentSpec.INITIAL_HYDROGEN_CONSENT_DOCUMENT,
      DocumentSpec.DEPOSIT_HYDROGEN_CONSENT_DOCUMENT,
      DocumentSpec.VARIATION_HYDROGEN_CONSENT_DOCUMENT,
      DocumentSpec.HUOO_HYDROGEN_CONSENT_DOCUMENT)
  ),
  CCUS_CONSENT_DOCUMENT(Set.of(
      DocumentSpec.INITIAL_CCUS_CONSENT_DOCUMENT,
      DocumentSpec.DEPOSIT_CCUS_CONSENT_DOCUMENT,
      DocumentSpec.VARIATION_CCUS_CONSENT_DOCUMENT,
      DocumentSpec.HUOO_CCUS_CONSENT_DOCUMENT)
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

  public static DocumentTemplateMnem getMnemFromResourceType(PwaResourceType resourceType) {
    switch (resourceType) {
      case PETROLEUM:
        return PETROLEUM_CONSENT_DOCUMENT;
      case HYDROGEN:
        return HYDROGEN_CONSENT_DOCUMENT;
      case CCUS:
        return CCUS_CONSENT_DOCUMENT;
      default:
        throw new PwaEntityNotFoundException(
            String.format("Could not find Resource Type of: %s", resourceType.getDisplayName()));
    }
  }

}
