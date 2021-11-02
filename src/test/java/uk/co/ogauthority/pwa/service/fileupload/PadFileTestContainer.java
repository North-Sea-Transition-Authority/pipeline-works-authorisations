package uk.co.ogauthority.pwa.service.fileupload;

import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;

public class PadFileTestContainer {

  private final PadFile padFile;

  private final UploadedFile uploadedFile;

  public PadFileTestContainer(PadFile padFile, UploadedFile uploadedFile) {
    this.padFile = padFile;
    this.uploadedFile = uploadedFile;
  }

  public PadFile getPadFile() {
    return padFile;
  }

  public UploadedFile getUploadedFile() {
    return uploadedFile;
  }
}
