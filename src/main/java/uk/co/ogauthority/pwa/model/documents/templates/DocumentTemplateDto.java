package uk.co.ogauthority.pwa.model.documents.templates;

import java.util.ArrayList;
import java.util.List;
import uk.co.ogauthority.pwa.model.documents.SectionDto;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;

public class DocumentTemplateDto {

  private DocumentTemplate documentTemplate;

  private List<SectionDto> sections;

  public DocumentTemplateDto() {
  }

  public DocumentTemplateDto(DocumentTemplate documentTemplate) {
    this.documentTemplate = documentTemplate;
    this.sections = new ArrayList<>();
  }

  public DocumentTemplate getDocumentTemplate() {
    return documentTemplate;
  }

  public void setDocumentTemplate(DocumentTemplate documentTemplate) {
    this.documentTemplate = documentTemplate;
  }

  public List<SectionDto> getSections() {
    return sections;
  }

  public void setSections(List<SectionDto> sections) {
    this.sections = sections;
  }
}
