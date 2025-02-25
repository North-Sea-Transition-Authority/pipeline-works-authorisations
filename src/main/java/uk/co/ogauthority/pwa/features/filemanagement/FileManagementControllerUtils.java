package uk.co.ogauthority.pwa.features.filemanagement;

import org.apache.commons.lang3.ObjectUtils;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

public class FileManagementControllerUtils {

  private FileManagementControllerUtils() {
    throw new AssertionError();
  }

  static boolean fileBelongsToUser(UploadedFile uploadedFile, AuthenticatedUserAccount user) {
    return uploadedFile.getUploadedBy().equals(String.valueOf(user.getWuaId()));
  }

  static boolean hasNoUsage(UploadedFile uploadedFile) {
    return ObjectUtils.allNull(uploadedFile.getUsageId(), uploadedFile.getUsageType(), uploadedFile.getDocumentType());
  }
}
