package uk.co.ogauthority.pwa.model.documents.view;

import java.util.ArrayList;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;

public class DocumentView {

  private PwaDocumentType documentType;

  private DocumentTemplateMnem documentTemplateMnem;

  private List<SectionView> sections;

  public DocumentView(PwaDocumentType documentType, DocumentTemplateMnem documentTemplateMnem) {
    this.documentTemplateMnem = documentTemplateMnem;
    this.documentType = documentType;
    this.sections = new ArrayList<>();
  }

  public PwaDocumentType getDocumentType() {
    return documentType;
  }

  public void setDocumentType(PwaDocumentType documentType) {
    this.documentType = documentType;
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
