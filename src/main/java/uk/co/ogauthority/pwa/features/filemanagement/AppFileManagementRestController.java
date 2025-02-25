package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;

@RestController
@RequestMapping("/pwa/{pwaId}/file-management")
class AppFileManagementRestController {

  private final FileService fileService;
  private final PwaApplicationService pwaApplicationService;
  private final AppFileManagementService appFileManagementService;

  AppFileManagementRestController(
      FileService fileService,
      PwaApplicationService pwaApplicationService,
      AppFileManagementService appFileManagementService
  ) {
    this.fileService = fileService;
    this.pwaApplicationService = pwaApplicationService;
    this.appFileManagementService = appFileManagementService;
  }

  @GetMapping("/download/{fileId}")
  ResponseEntity<InputStreamResource> download(
      @PathVariable Integer pwaId,
      @PathVariable UUID fileId
  ) {
    var pwaApplication = pwaApplicationService.getApplicationFromId(pwaId);

    var file = fileService.find(fileId)
        .orElseThrow(() -> appFileManagementService.getFileNotFoundException(fileId, pwaApplication));

    appFileManagementService.throwIfFileDoesNotBelongToApplication(file, pwaApplication);

    return fileService.download(file);
  }

  @PostMapping("/delete/{fileId}")
  FileDeleteResponse delete(
      @PathVariable Integer pwaId,
      @PathVariable UUID fileId
  ) {
    var pwaApplication = pwaApplicationService.getApplicationFromId(pwaId);

    var file = fileService.find(fileId)
        .orElseThrow(() -> appFileManagementService.getFileNotFoundException(fileId, pwaApplication));

    appFileManagementService.throwIfFileDoesNotBelongToApplication(file, pwaApplication);

    return fileService.delete(file);
  }

}