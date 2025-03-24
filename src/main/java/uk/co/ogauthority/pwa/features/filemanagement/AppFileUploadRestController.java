package uk.co.ogauthority.pwa.features.filemanagement;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;

@RestController
@RequestMapping("/pwa/{pwaId}/file-management")
public class AppFileUploadRestController {

  private final FileService fileService;
  private final PwaApplicationService pwaApplicationService;
  private final AppFileService appFileService;

  AppFileUploadRestController(
      FileService fileService,
      PwaApplicationService pwaApplicationService,
      AppFileService appFileService
  ) {
    this.fileService = fileService;
    this.pwaApplicationService = pwaApplicationService;
    this.appFileService = appFileService;
  }

  @PostMapping("/{legacyFilePurpose}")
  public FileUploadResponse upload(
      @PathVariable Integer pwaId,
      @PathVariable String legacyFilePurpose,
      MultipartFile file
  ) {
    var purpose = AppFilePurpose.valueOf(legacyFilePurpose);

    var pwaApplication = pwaApplicationService.getApplicationFromId(pwaId);

    var response = fileService.upload(builder -> builder
        .withFileSource(FileSource.fromMultipartFile(file))
        .build());

    appFileService.processInitialUpload(response, pwaApplication, purpose);

    return response;
  }
}