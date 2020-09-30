package uk.co.ogauthority.pwa.model.documents.view;

import java.util.ArrayList;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;

public class DocumentView {

  private DocumentTemplate documentTemplate;

  private String docType;

  private List<SectionView> sections;

  public DocumentView() {
  }

  public DocumentView(DocumentTemplate documentTemplate) {
    this.documentTemplate = documentTemplate;
    this.sections = new ArrayList<>();
  }

  public DocumentTemplate getDocumentTemplate() {
    return documentTemplate;
  }

  public void setDocumentTemplate(DocumentTemplate documentTemplate) {
    this.documentTemplate = documentTemplate;
  }

  public List<SectionView> getSections() {
    return sections;
  }

  public void setSections(List<SectionView> sections) {
    this.sections = sections;
  }

}
