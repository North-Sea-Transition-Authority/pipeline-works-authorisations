package uk.co.ogauthority.pwa.model.entity.mailmerge;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;

@Entity
@Table(name = "mail_merge_field_doc_specs")
public class MailMergeFieldDocSpec {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "mnem", name = "mail_merge_field_mnem")
  private MailMergeField mailMergeField;

  @Column(name = "doc_spec_mnem")
  @Enumerated(EnumType.STRING)
  private DocumentSpec documentSpec;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public MailMergeField getMailMergeField() {
    return mailMergeField;
  }

  public void setMailMergeField(MailMergeField mailMergeField) {
    this.mailMergeField = mailMergeField;
  }

  public DocumentSpec getDocumentSpec() {
    return documentSpec;
  }

  public void setDocumentSpec(DocumentSpec documentSpec) {
    this.documentSpec = documentSpec;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MailMergeFieldDocSpec that = (MailMergeFieldDocSpec) o;
    return Objects.equals(id, that.id) && Objects.equals(mailMergeField,
        that.mailMergeField) && documentSpec == that.documentSpec;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, mailMergeField, documentSpec);
  }
}
