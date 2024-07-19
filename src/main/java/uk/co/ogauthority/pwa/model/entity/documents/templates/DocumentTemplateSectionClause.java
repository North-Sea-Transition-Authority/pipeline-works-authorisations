package uk.co.ogauthority.pwa.model.entity.documents.templates;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
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

  public void setDocumentTemplateSection(DocumentTemplateSection documentTemplateSection) {
    this.documentTemplateSection = documentTemplateSection;
  }

  @Override
  public DocumentTemplateSection getSection() {
    return getDocumentTemplateSection();
  }

  @Override
  public void setSection(DocumentTemplateSection section) {
    setDocumentTemplateSection(section);
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
