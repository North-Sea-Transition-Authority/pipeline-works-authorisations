package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

@Service
public class ConsentDocumentFileManagementService {

  private final FileManagementService fileManagementService;
  private final FileService fileService;

  public ConsentDocumentFileManagementService(
      FileManagementService fileManagementService,
      FileService fileService
  ) {
    this.fileManagementService = fileManagementService;
    this.fileService = fileService;
  }

  public void saveConsentDocument(
      UploadedFileForm uploadedFileForm,
      PwaConsent pwaConsent
  ) {
    fileManagementService.saveFiles(
        List.of(uploadedFileForm),
        getUsageId(pwaConsent),
        getUsageType(),
        FileDocumentType.CONSENT_DOCUMENT
    );
  }

  public List<UploadedFile> getUploadedConsentDocuments(PwaConsent pwaConsent) {
    return fileService.findAll(getUsageId(pwaConsent), getUsageType(), FileDocumentType.CONSENT_DOCUMENT.name());
  }

  private String getUsageId(PwaConsent pwaConsent) {
    return String.valueOf(pwaConsent.getId());
  }

  private String getUsageType() {
    return PwaConsent.class.getSimpleName();
  }
}
