package uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
