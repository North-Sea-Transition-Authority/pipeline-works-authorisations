package uk.co.ogauthority.pwa.model.entity.documents.templates;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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
