package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;


public class PermanentDepositDrawingView {

  private String reference;
  private String documentDescription;
  private String fileId;
  private String fileName;
  private Set<String> depositReferences;

  public PermanentDepositDrawingView(String reference, Set<String> depositReferences, UploadedFileView uploadedFileView) {
    this.reference = reference;
    this.depositReferences = depositReferences;
    this.documentDescription = uploadedFileView.getFileDescription();
    this.fileId = uploadedFileView.getFileId();
    this.fileName = uploadedFileView.getFileName();
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getDocumentDescription() {
    return documentDescription;
  }

  public void setDocumentDescription(String documentDescription) {
    this.documentDescription = documentDescription;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Set<String> getDepositReferences() {
    return depositReferences;
  }

  public void setDepositReferences(Set<String> depositReferences) {
    this.depositReferences = depositReferences;
  }
}
