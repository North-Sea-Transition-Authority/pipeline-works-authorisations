package uk.co.ogauthority.pwa.model.entity.documents.instances;

import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClauseVersion;

@Entity
@Table(name = "di_sc_versions")
public class DocumentInstanceSectionClauseVersion extends SectionClauseVersion {

  // custom sequence generator used to allow caching of batches of sequence values when inserting clause versions to aid performance.
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "di_section_clause_version_id_generator")
  @GenericGenerator(
      name = "di_section_clause_version_id_generator",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
          @Parameter(name = "sequence_name", value = "di_sc_versions_id_seq"),
          @Parameter(name = "optimizer", value = "pooled"),
          @Parameter(name = "initial_value", value = "1"),
          @Parameter(name = "increment_size", value = "100")
      }
  )
  private Integer id;

  @ManyToOne
  @JoinColumn(name =  "di_sc_id")
  private DocumentInstanceSectionClause documentInstanceSectionClause;

  @OneToOne
  @JoinColumn(name = "parent_di_sc_id")
  private DocumentInstanceSectionClause parentDocumentInstanceSectionClause;

  public DocumentInstanceSectionClauseVersion() {
  }

  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public SectionClause getClause() {
    return getDocumentInstanceSectionClause();
  }

  @Override
  public void setClause(SectionClause clause) {
    this.documentInstanceSectionClause = (DocumentInstanceSectionClause) clause;
  }

  public DocumentInstanceSectionClause getDocumentInstanceSectionClause() {
    return documentInstanceSectionClause;
  }

  public void setDocumentInstanceSectionClause(DocumentInstanceSectionClause documentInstanceSectionClause) {
    this.documentInstanceSectionClause = documentInstanceSectionClause;
  }

  @Override
  public Optional<SectionClause> getParentClause() {
    return Optional.ofNullable(parentDocumentInstanceSectionClause);
  }

  @Override
  public void setParentClause(SectionClause parentClause) {
    this.parentDocumentInstanceSectionClause = (DocumentInstanceSectionClause) parentClause;
  }

  public DocumentInstanceSectionClause getParentDocumentInstanceSectionClause() {
    return parentDocumentInstanceSectionClause;
  }

  public void setParentDocumentInstanceSectionClause(DocumentInstanceSectionClause parentDocumentInstanceSectionClause) {
    this.parentDocumentInstanceSectionClause = parentDocumentInstanceSectionClause;
  }

}
