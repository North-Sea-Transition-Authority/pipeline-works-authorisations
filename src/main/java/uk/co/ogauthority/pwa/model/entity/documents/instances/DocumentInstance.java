package uk.co.ogauthority.pwa.model.entity.documents.instances;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;

@Entity
@Table(name = "document_instances")
public class DocumentInstance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name =  "dt_id")
  private DocumentTemplate documentTemplate;

  @ManyToOne
  @JoinColumn(name = "pwa_application_id")
  private PwaApplication pwaApplication;

  private Instant createdTimestamp;

  public DocumentInstance() {
  }

  public DocumentInstance(DocumentTemplate documentTemplate,
                          PwaApplication pwaApplication,
                          Instant createdTimestamp) {
    this.documentTemplate = documentTemplate;
    this.pwaApplication = pwaApplication;
    this.createdTimestamp = createdTimestamp;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DocumentTemplate getDocumentTemplate() {
    return documentTemplate;
  }

  public void setDocumentTemplate(DocumentTemplate documentTemplate) {
    this.documentTemplate = documentTemplate;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public void setPwaApplication(PwaApplication pwaApplication) {
    this.pwaApplication = pwaApplication;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocumentInstance that = (DocumentInstance) o;
    return Objects.equals(id, that.id) && Objects.equals(documentTemplate,
        that.documentTemplate) && Objects.equals(pwaApplication,
        that.pwaApplication) && Objects.equals(createdTimestamp, that.createdTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, documentTemplate, pwaApplication, createdTimestamp);
  }
}
