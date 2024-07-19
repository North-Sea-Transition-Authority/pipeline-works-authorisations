package uk.co.ogauthority.pwa.model.entity.documents.instances;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.Optional;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;

@Entity
@Table(name = "di_section_clauses")
public class DocumentInstanceSectionClause implements SectionClause {

  // custom sequence generator used to allow caching of batches of sequence values when inserting section clauses to aid performance.
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "di_section_clause_id_generator")
  @GenericGenerator(
      name = "di_section_clause_id_generator",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
          @Parameter(name = "sequence_name", value = "di_section_clauses_id_seq"),
          @Parameter(name = "optimizer", value = "pooled"),
          @Parameter(name = "initial_value", value = "1"),
          @Parameter(name = "increment_size", value = "100")
      }
  )
  private Integer id;

  @ManyToOne
  @JoinColumn(name =  "di_id")
  private DocumentInstance documentInstance;

  @OneToOne
  @JoinColumn(name = "dt_sc_id")
  private DocumentTemplateSectionClause documentTemplateSectionClause;

  @OneToOne
  @JoinColumn(name = "dt_s_id")
  private DocumentTemplateSection documentTemplateSection;

  public DocumentInstanceSectionClause() {
  }

  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DocumentInstance getDocumentInstance() {
    return documentInstance;
  }

  public void setDocumentInstance(DocumentInstance documentInstance) {
    this.documentInstance = documentInstance;
  }

  public Optional<DocumentTemplateSectionClause> getDocumentTemplateSectionClause() {
    return Optional.ofNullable(documentTemplateSectionClause);
  }

  public void setDocumentTemplateSectionClause(
      DocumentTemplateSectionClause documentTemplateSectionClause) {
    this.documentTemplateSectionClause = documentTemplateSectionClause;
  }

  public Optional<DocumentTemplateSection> getDocumentTemplateSection() {
    return Optional.ofNullable(documentTemplateSection);
  }

  public void setDocumentTemplateSection(
      DocumentTemplateSection documentTemplateSection) {
    this.documentTemplateSection = documentTemplateSection;
  }

  @Override
  public DocumentTemplateSection getSection() {

    Optional<DocumentTemplateSectionClause> docTemplateClauseOpt = getDocumentTemplateSectionClause();

    if (docTemplateClauseOpt.isPresent()) {
      return docTemplateClauseOpt.get().getDocumentTemplateSection();
    }

    Optional<DocumentTemplateSection> sectionOpt = getDocumentTemplateSection();

    if (sectionOpt.isPresent()) {
      return sectionOpt.get();
    }

    throw new IllegalStateException(String.format("Doc instance section clause with id [%s] has no section", id));

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
    DocumentInstanceSectionClause that = (DocumentInstanceSectionClause) o;
    return Objects.equals(id, that.id)
        && Objects.equals(documentInstance, that.documentInstance)
        && Objects.equals(documentTemplateSectionClause, that.documentTemplateSectionClause);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, documentInstance, documentTemplateSectionClause);
  }
}
