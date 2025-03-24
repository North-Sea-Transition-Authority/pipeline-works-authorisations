package uk.co.ogauthority.pwa.service.fileupload;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadFileManagementTestUtil {

  // no instantiation
  private PadFileManagementTestUtil() {

  }

  public static MultipartFile createRandomMultipartFile() {
    var randomFileName = UUID.randomUUID() + ".jpeg";

    return new MockMultipartFile(randomFileName, randomFileName, "image/jpeg", new byte[0]);
  }

  public static UploadedFile createUploadedFile(PwaApplicationDetail pwaApplicationDetail,
                                                FileDocumentType fileDocumentType) {
    byte[] array = new byte[7]; // length is bounded by 7
    new Random().nextBytes(array);
    String generalPurposeRandomString = new String(array, StandardCharsets.UTF_8);
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(UUID.randomUUID());
    uploadedFile.setName(generalPurposeRandomString);
    uploadedFile.setDescription(generalPurposeRandomString);
    uploadedFile.setContentLength(0L);
    uploadedFile.setUploadedAt(Instant.now());
    uploadedFile.setUsageId(pwaApplicationDetail.getId().toString());
    uploadedFile.setUsageType(PwaApplicationDetail.class.getSimpleName());
    uploadedFile.setDocumentType(fileDocumentType.toString());

    return uploadedFile;
  }

  public static PadFile createPadFileWithRandomFileIdAndData(PwaApplicationDetail pwaApplicationDetail,
                                                                          UUID fileId,
                                                                          ApplicationDetailFilePurpose applicationDetailFilePurpose) {
    var padFile = new PadFile(
        pwaApplicationDetail,
        String.valueOf(fileId),
        applicationDetailFilePurpose,
        ApplicationFileLinkStatus.FULL);
    padFile.setDescription("description");

    return padFile;
  }


}
