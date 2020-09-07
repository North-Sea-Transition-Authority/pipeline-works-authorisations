package uk.co.ogauthority.pwa.model.form.appprocessing.casenotes;

import javax.validation.constraints.NotNull;

public class AddCaseNoteForm {

  @NotNull(message = "Enter some note text")
  private String noteText;

  public AddCaseNoteForm() {
  }

  public String getNoteText() {
    return noteText;
  }

  public void setNoteText(String noteText) {
    this.noteText = noteText;
  }
}
