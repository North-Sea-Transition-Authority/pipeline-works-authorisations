package uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casehistory.CaseHistoryItem;
import uk.co.ogauthority.pwa.model.entity.enums.appprocessing.casehistory.CaseHistoryItemType;

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
