package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.List;
import java.util.Set;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;

public class FileManagementControllerTestUtils {

  public static FileUploadComponentAttributes createUploadFileAttributes() {
    return FileUploadComponentAttributes.newBuilder()
        .withMaximumSize(DataSize.of(50, DataUnit.MEGABYTES))
        .withAllowedExtensions(Set.of("png", "jpg"))
        .withUploadUrl("upload")
        .withDownloadUrl("download")
        .withDeleteUrl("delete")
        .withExistingFiles(List.of())
        .build();
  }

}
