package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import java.util.Set;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;


public class PermanentDepositDrawingView {

  private Integer depositDrawingId;
  private String reference;
  private String documentDescription;
  private String fileId;
  private String fileName;
  private Set<String> depositReferences;


  public PermanentDepositDrawingView() {
  }

  public PermanentDepositDrawingView(Integer depositDrawingId, String reference,
                                     Set<String> depositReferences) {
    this.depositDrawingId = depositDrawingId;
    this.reference = reference;
    this.depositReferences = depositReferences;
  }

  public PermanentDepositDrawingView(Integer depositDrawingId, String reference,
                                     Set<String> depositReferences, UploadedFileView uploadedFileView) {
    this.depositDrawingId = depositDrawingId;
    this.reference = reference;
    this.depositReferences = depositReferences;
    this.documentDescription = uploadedFileView != null ? uploadedFileView.getFileDescription() : "";
    this.fileId = uploadedFileView != null ? uploadedFileView.getFileId() : "";
    this.fileName = uploadedFileView != null ? uploadedFileView.getFileName() : "";
  }

  public Integer getDepositDrawingId() {
    return depositDrawingId;
  }

  public void setDepositDrawingId(Integer depositDrawingId) {
    this.depositDrawingId = depositDrawingId;
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
