package uk.co.ogauthority.pwa.service.fileupload;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Random;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadFileTestUtil {

  // no instantiation
  private PadFileTestUtil() {

  }

  public static PadFileTestContainer createPadFileWithRandomFileIdAndData(PwaApplicationDetail pwaApplicationDetail,
                                                                          ApplicationDetailFilePurpose applicationDetailFilePurpose) {
    byte[] array = new byte[7]; // length is bounded by 7
    new Random().nextBytes(array);
    String generalPurposeRandomString = new String(array, Charset.forName("UTF-8"));
    var uploadedFile = new UploadedFile(
        generalPurposeRandomString,
        generalPurposeRandomString,
        generalPurposeRandomString,
        0L,
        Instant.now(),
        FileUploadStatus.CURRENT);

    var padFile = new PadFile(
        pwaApplicationDetail,
        generalPurposeRandomString,
        applicationDetailFilePurpose,
        ApplicationFileLinkStatus.FULL);
    padFile.setDescription(generalPurposeRandomString);

    return new PadFileTestContainer(padFile, uploadedFile);
  }


}
