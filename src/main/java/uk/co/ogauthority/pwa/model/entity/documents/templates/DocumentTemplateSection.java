package uk.co.ogauthority.pwa.model.entity.documents.templates;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.ogauthority.pwa.model.enums.documents.DocumentTemplateSectionStatus;

@Entity
@Table(name = "dt_sections")
public class DocumentTemplateSection {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name =  "dt_id")
  private DocumentTemplate documentTemplate;

  private String name;

  @Enumerated(EnumType.STRING)
  private DocumentTemplateSectionStatus status;

  private Instant startTimestamp;

  private Instant endTimestamp;

  public DocumentTemplateSection() {
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DocumentTemplateSectionStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentTemplateSectionStatus status) {
    this.status = status;
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(Instant startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public Instant getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(Instant endTimestamp) {
    this.endTimestamp = endTimestamp;
  }
}
