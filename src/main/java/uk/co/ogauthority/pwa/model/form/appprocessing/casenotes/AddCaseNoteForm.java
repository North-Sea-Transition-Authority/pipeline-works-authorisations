package uk.co.ogauthority.pwa.model.form.appprocessing.casenotes;

import uk.co.ogauthority.pwa.features.filemanagement.FileUploadForm;

public class AddCaseNoteForm extends FileUploadForm {

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
