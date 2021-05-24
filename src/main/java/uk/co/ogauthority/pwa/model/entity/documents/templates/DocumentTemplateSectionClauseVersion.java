package uk.co.ogauthority.pwa.model.entity.documents.templates;

import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClauseVersion;

@Entity
@Table(name = "dt_section_clause_versions")
public class DocumentTemplateSectionClauseVersion extends SectionClauseVersion {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "dt_section_clause_version_id_generator")
  @SequenceGenerator(name = "dt_section_clause_version_id_generator", sequenceName = "dt_scv_id_seq", allocationSize = 1)
  private Integer id;

  @ManyToOne
  @JoinColumn(name =  "sc_id")
  private DocumentTemplateSectionClause documentTemplateSectionClause;

  @OneToOne
  @JoinColumn(name = "parent_sc_id")
  private DocumentTemplateSectionClause parentDocumentTemplateSectionClause;

  public DocumentTemplateSectionClauseVersion() {
  }

  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public DocumentTemplateSectionClause getClause() {
    return documentTemplateSectionClause;
  }

  @Override
  public void setClause(SectionClause clause) {
    this.documentTemplateSectionClause = (DocumentTemplateSectionClause) clause;
  }

  public DocumentTemplateSectionClause getDocumentTemplateSectionClause() {
    return getClause();
  }

  public void setDocumentTemplateSectionClause(DocumentTemplateSectionClause documentTemplateSectionClause) {
    this.documentTemplateSectionClause = documentTemplateSectionClause;
  }

  @Override
  public Optional<SectionClause> getParentClause() {
    return Optional.ofNullable(parentDocumentTemplateSectionClause);
  }

  @Override
  public void setParentClause(SectionClause parentClause) {
    this.parentDocumentTemplateSectionClause = (DocumentTemplateSectionClause) parentClause;
  }

  public void setParentDocumentTemplateSectionClause(DocumentTemplateSectionClause parentDocumentTemplateSectionClause) {
    this.parentDocumentTemplateSectionClause = parentDocumentTemplateSectionClause;
  }

  public DocumentTemplateSectionClause getParentDocumentTemplateSectionClause() {
    return parentDocumentTemplateSectionClause;
  }

}
