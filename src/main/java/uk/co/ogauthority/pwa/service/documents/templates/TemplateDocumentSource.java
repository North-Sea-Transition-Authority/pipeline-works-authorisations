package uk.co.ogauthority.pwa.service.documents.templates;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;

public class TemplateDocumentSource implements DocumentSource {

  private DocumentTemplateMnem documentTemplateMnem;

  private DocumentSpec documentSpec;

  public TemplateDocumentSource(
      DocumentTemplateMnem documentTemplateMnem,
      DocumentSpec documentSpec) {
    this.documentTemplateMnem = documentTemplateMnem;
    this.documentSpec = documentSpec;
  }

  public DocumentTemplateMnem getDocumentTemplateMnem() {
    return documentTemplateMnem;
  }

  public void setDocumentTemplateMnem(
      DocumentTemplateMnem documentTemplateMnem) {
    this.documentTemplateMnem = documentTemplateMnem;
  }

  @Override
  public Object getSource() {
    return documentTemplateMnem;
  }

  @Override
  public DocumentSpec getDocumentSpec() {
    return documentSpec;
  }

  public void setDocumentSpec(DocumentSpec documentSpec) {
    this.documentSpec = documentSpec;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateDocumentSource that = (TemplateDocumentSource) o;
    return documentTemplateMnem == that.documentTemplateMnem && documentSpec == that.documentSpec;
  }

  @Override
  public int hashCode() {
    return Objects.hash(documentTemplateMnem, documentSpec);
  }

}
