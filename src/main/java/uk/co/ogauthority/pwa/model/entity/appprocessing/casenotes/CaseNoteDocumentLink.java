package uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;

@Entity
@Table(name = "case_note_document_links")
public class CaseNoteDocumentLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "case_note_id")
  private CaseNote caseNote;

  @OneToOne
  @JoinColumn(name = "af_id")
  private AppFile appFile;

  public CaseNoteDocumentLink() {
  }

  public CaseNoteDocumentLink(CaseNote caseNote, AppFile appFile) {
    this.caseNote = caseNote;
    this.appFile = appFile;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public CaseNote getCaseNote() {
    return caseNote;
  }

  public void setCaseNote(CaseNote caseNote) {
    this.caseNote = caseNote;
  }

  public AppFile getAppFile() {
    return appFile;
  }

  public void setAppFile(AppFile appFile) {
    this.appFile = appFile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CaseNoteDocumentLink that = (CaseNoteDocumentLink) o;
    return Objects.equals(id, that.id)
        && Objects.equals(caseNote, that.caseNote)
        && Objects.equals(appFile, that.appFile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, caseNote, appFile);
  }
}
