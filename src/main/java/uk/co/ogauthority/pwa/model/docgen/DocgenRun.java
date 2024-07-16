package uk.co.ogauthority.pwa.model.docgen;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Blob;
import java.time.Instant;
import java.util.Objects;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;

@Entity
@Table(name = "docgen_runs")
public class DocgenRun {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToOne
  @JoinColumn(name = "di_id")
  private DocumentInstance documentInstance;

  @Enumerated(EnumType.STRING)
  @Column(name = "docgen_type")
  private DocGenType docGenType;

  @Enumerated(EnumType.STRING)
  private DocgenRunStatus status;

  @ManyToOne
  @JoinColumn(name = "scheduled_by_person_id")
  private Person scheduledByPerson;

  private Instant scheduledOn;

  private Instant startedOn;

  private Instant completedOn;

  @Lob
  @Column(name = "generated_doc")
  private Blob generatedDocument;

  public DocgenRun() {

  }

  public DocgenRun(DocumentInstance documentInstance,
                   DocGenType docGenType,
                   DocgenRunStatus status) {
    this.documentInstance = documentInstance;
    this.docGenType = docGenType;
    this.status = status;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public DocumentInstance getDocumentInstance() {
    return documentInstance;
  }

  public void setDocumentInstance(DocumentInstance documentInstance) {
    this.documentInstance = documentInstance;
  }

  public DocGenType getDocGenType() {
    return docGenType;
  }

  public void setDocGenType(DocGenType docGenType) {
    this.docGenType = docGenType;
  }

  public DocgenRunStatus getStatus() {
    return status;
  }

  public void setStatus(DocgenRunStatus status) {
    this.status = status;
  }

  public Person getScheduledByPerson() {
    return scheduledByPerson;
  }

  public void setScheduledByPerson(Person scheduledByPerson) {
    this.scheduledByPerson = scheduledByPerson;
  }

  public Instant getScheduledOn() {
    return scheduledOn;
  }

  public void setScheduledOn(Instant scheduledOn) {
    this.scheduledOn = scheduledOn;
  }

  public Instant getStartedOn() {
    return startedOn;
  }

  public void setStartedOn(Instant startedOn) {
    this.startedOn = startedOn;
  }

  public Instant getCompletedOn() {
    return completedOn;
  }

  public void setCompletedOn(Instant completedOn) {
    this.completedOn = completedOn;
  }

  public Blob getGeneratedDocument() {
    return generatedDocument;
  }

  public void setGeneratedDocument(Blob generatedDocument) {
    this.generatedDocument = generatedDocument;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocgenRun docgenRun = (DocgenRun) o;
    return id == docgenRun.id && Objects.equals(documentInstance,
        docgenRun.documentInstance) && docGenType == docgenRun.docGenType && status == docgenRun.status && Objects.equals(
        scheduledByPerson, docgenRun.scheduledByPerson) && Objects.equals(scheduledOn,
        docgenRun.scheduledOn) && Objects.equals(startedOn, docgenRun.startedOn) && Objects.equals(
        completedOn, docgenRun.completedOn) && Objects.equals(generatedDocument, docgenRun.generatedDocument);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, documentInstance, docGenType, status, scheduledByPerson, scheduledOn, startedOn,
        completedOn,
        generatedDocument);
  }
}
