package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

public abstract class FileUploadForm {

  private List<UploadedFileForm> uploadedFiles = new ArrayList<>();

  public List<UploadedFileForm> getUploadedFiles() {
    return uploadedFiles;
  }

  public void setUploadedFiles(List<UploadedFileForm> uploadedFiles) {
    this.uploadedFiles = uploadedFiles;
  }
}
