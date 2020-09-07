package uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casehistory.CaseHistoryItem;
import uk.co.ogauthority.pwa.model.entity.enums.appprocessing.casehistory.CaseHistoryItemType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

@Entity
@Table(name = "case_notes")
public class CaseNote extends CaseHistoryItem {

  private String noteText;

  public CaseNote() {
  }

  public CaseNote(PwaApplication pwaApplication,
                  PersonId creatingPersonId,
                  Instant creationDateTime,
                  String noteText) {
    super(pwaApplication, CaseHistoryItemType.CASE_NOTE, creatingPersonId, creationDateTime);
    this.noteText = noteText;
  }

  public String getNoteText() {
    return noteText;
  }

  public void setNoteText(String noteText) {
    this.noteText = noteText;
  }
}
