package uk.co.ogauthority.pwa.model.entity.documents.templates;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClause;

@Entity
@Table(name = "dt_section_clauses")
public class DocumentTemplateSectionClause implements SectionClause {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "dt_section_clause_id_generator")
  @SequenceGenerator(name = "dt_section_clause_id_generator", sequenceName = "dt_section_clauses_id_seq", allocationSize = 1)
  private Integer id;

  @ManyToOne
  @JoinColumn(name =  "s_id")
  private DocumentTemplateSection documentTemplateSection;

  public DocumentTemplateSectionClause() {
  }

  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DocumentTemplateSection getDocumentTemplateSection() {
    return documentTemplateSection;
  }

  public void setDocumentTemplateSection(
      DocumentTemplateSection documentTemplateSection) {
    this.documentTemplateSection = documentTemplateSection;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocumentTemplateSectionClause that = (DocumentTemplateSectionClause) o;
    return Objects.equals(id, that.id)
        && Objects.equals(documentTemplateSection, that.documentTemplateSection);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, documentTemplateSection);
  }
}
