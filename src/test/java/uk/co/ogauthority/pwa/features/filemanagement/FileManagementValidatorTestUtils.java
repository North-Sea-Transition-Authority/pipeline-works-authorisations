package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.UUID;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

public class FileManagementValidatorTestUtils {

  private FileManagementValidatorTestUtils() { throw new AssertionError(); }

  public static UploadedFileForm createUploadedFileForm() {
    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(UUID.randomUUID());
    uploadedFileForm.setFileDescription("Description");

    return uploadedFileForm;
  }

  public static UploadedFileForm createUploadedFileFormWithoutDescription() {
    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileId(UUID.randomUUID());

    return uploadedFileForm;
  }

}
