package uk.co.ogauthority.pwa.model.documents.view;

import java.util.ArrayList;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;

public class DocumentView {

  private PwaDocumentType documentType;

  private DocumentSource documentSource;

  private DocumentTemplateMnem documentTemplateMnem;

  private List<SectionView> sections;

  public DocumentView(PwaDocumentType documentType,
                      DocumentSource documentSource,
                      DocumentTemplateMnem documentTemplateMnem) {
    this.documentType = documentType;
    this.documentTemplateMnem = documentTemplateMnem;
    this.documentSource = documentSource;
    this.sections = new ArrayList<>();
  }

  public PwaDocumentType getDocumentType() {
    return documentType;
  }

  public void setDocumentType(PwaDocumentType documentType) {
    this.documentType = documentType;
  }

  public DocumentSource getDocumentSource() {
    return documentSource;
  }

  public void setDocumentSource(DocumentSource documentSource) {
    this.documentSource = documentSource;
  }

  public DocumentTemplateMnem getDocumentTemplateMnem() {
    return documentTemplateMnem;
  }

  public void setDocumentTemplateMnem(DocumentTemplateMnem documentTemplateMnem) {
    this.documentTemplateMnem = documentTemplateMnem;
  }

  public List<SectionView> getSections() {
    return sections;
  }

  public void setSections(List<SectionView> sections) {
    this.sections = sections;
  }

}
