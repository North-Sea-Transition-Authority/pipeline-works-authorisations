package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.filehelper.FileUploadTestHarnessUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class PadFileManagementTestHarnessService {

  private final FileService fileService;
  private final PadFileManagementService padFileManagementService;
  private final PadFileService padFileService;

  public PadFileManagementTestHarnessService(FileService fileService,
                                             PadFileManagementService padFileManagementService,
                                             PadFileService padFileService
  ) {
    this.fileService = fileService;
    this.padFileManagementService = padFileManagementService;
    this.padFileService = padFileService;
  }

  public void uploadFileAndMapToForm(FileUploadForm form, PwaApplicationDetail pwaApplicationDetail, FileDocumentType fileDocumentType) {
    var response = fileService.upload(builder -> builder
        .withFileSource(FileSource.fromMultipartFile(FileUploadTestHarnessUtil.getSampleMultipartFile()))
        .build());

    var fileForm = new UploadedFileForm();
    fileForm.setFileId(response.getFileId());
    fileForm.setFileName(response.getFileName());
    fileForm.setFileDescription(FileUploadTestHarnessUtil.getFileDescription());

    form.setUploadedFiles(List.of(fileForm));

    padFileManagementService.saveFiles(form, pwaApplicationDetail, fileDocumentType);
    padFileManagementService.mapFilesToForm(form, pwaApplicationDetail, fileDocumentType);
  }

  public void uploadFileAndMapToFormWithLegacyPadFileLink(
      FileUploadForm form,
      PwaApplicationDetail pwaApplicationDetail,
      FileDocumentType fileDocumentType,
      ApplicationDetailFilePurpose applicationDetailFilePurpose
  ) {
    var response = fileService.upload(builder -> builder
        .withFileSource(FileSource.fromMultipartFile(FileUploadTestHarnessUtil.getSampleMultipartFile()))
        .build());

    var fileForm = new UploadedFileForm();
    fileForm.setFileId(response.getFileId());
    fileForm.setFileName(response.getFileName());
    fileForm.setFileDescription(FileUploadTestHarnessUtil.getFileDescription());

    form.setUploadedFiles(List.of(fileForm));

    padFileManagementService.saveFiles(form, pwaApplicationDetail, fileDocumentType);
    padFileService.savePadFileIfValid(response, pwaApplicationDetail, applicationDetailFilePurpose);
    padFileManagementService.mapFilesToForm(form, pwaApplicationDetail, fileDocumentType);
  }
}
