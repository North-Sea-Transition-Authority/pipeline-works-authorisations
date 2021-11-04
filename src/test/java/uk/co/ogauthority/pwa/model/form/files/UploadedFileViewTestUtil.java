package uk.co.ogauthority.pwa.model.form.files;

import java.time.LocalDate;
import java.time.ZoneId;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;

public class UploadedFileViewTestUtil {

  private UploadedFileViewTestUtil() {
    // no instantiation
  }

  public static UploadedFileView createDefaultFileView() {
    return new UploadedFileView(
        "FILE_ID",
        "FILE_NAME",
        100L,
        "FILE_DESC",
        LocalDate.of(2020, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        "FILE_URL"
    );
  }

}