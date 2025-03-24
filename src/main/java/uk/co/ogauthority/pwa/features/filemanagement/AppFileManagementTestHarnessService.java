package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.filehelper.FileUploadTestHarnessUtil;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;

@Service
public class AppFileManagementTestHarnessService {

  private final FileService fileService;
  private final AppFileManagementService appFileManagementService;
  private final AppFileService appFileService;

  public AppFileManagementTestHarnessService(FileService fileService,
                                             AppFileManagementService appFileManagementService,
                                             AppFileService appFileService
  ) {
    this.fileService = fileService;
    this.appFileManagementService = appFileManagementService;
    this.appFileService = appFileService;
  }

  public void uploadFileAndMapToForm(
      FileUploadForm form,
      PwaApplication pwaApplication,
      FileDocumentType fileDocumentType,
      AppFilePurpose appFilePurpose
  ) {
    var response = fileService.upload(builder -> builder
        .withFileSource(FileSource.fromMultipartFile(FileUploadTestHarnessUtil.getSampleMultipartFile()))
        .build());

    var fileForm = new UploadedFileForm();
    fileForm.setFileId(response.getFileId());
    fileForm.setFileName(response.getFileName());
    fileForm.setFileDescription(FileUploadTestHarnessUtil.getFileDescription());

    form.setUploadedFiles(List.of(fileForm));

    appFileManagementService.saveFiles(form, pwaApplication, fileDocumentType);
    appFileService.processInitialUpload(response, pwaApplication, appFilePurpose);
    appFileManagementService.mapFilesToForm(form, pwaApplication, fileDocumentType);
  }
}
