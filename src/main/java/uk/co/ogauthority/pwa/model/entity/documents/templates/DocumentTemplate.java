package uk.co.ogauthority.pwa.model.entity.documents.templates;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;

@Entity
@Table(name = "document_templates")
public class DocumentTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  private DocumentTemplateMnem mnem;

  public DocumentTemplate() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DocumentTemplateMnem getMnem() {
    return mnem;
  }

  public void setMnem(DocumentTemplateMnem mnem) {
    this.mnem = mnem;
  }
}
