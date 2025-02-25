package uk.co.ogauthority.pwa.service.fileupload;

import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFileOld;

public class PadFileTestContainer {

  private final PadFile padFile;

  private final UploadedFileOld uploadedFile;

  public PadFileTestContainer(PadFile padFile, UploadedFileOld uploadedFile) {
    this.padFile = padFile;
    this.uploadedFile = uploadedFile;
  }

  public PadFile getPadFile() {
    return padFile;
  }

  public UploadedFileOld getUploadedFile() {
    return uploadedFile;
  }
}
