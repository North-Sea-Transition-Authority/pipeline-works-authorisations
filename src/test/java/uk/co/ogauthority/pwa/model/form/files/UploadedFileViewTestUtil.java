package uk.co.ogauthority.pwa.model.form.files;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;

public class UploadedFileViewTestUtil {

  private UploadedFileViewTestUtil() {
    // no instantiation
  }

  public static UploadedFileView createDefaultFileView() {
    return new UploadedFileView(
        String.valueOf(UUID.randomUUID()),
        "FILE_NAME",
        100L,
        "FILE_DESC",
        LocalDate.of(2020, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        "FILE_URL"
    );
  }

}