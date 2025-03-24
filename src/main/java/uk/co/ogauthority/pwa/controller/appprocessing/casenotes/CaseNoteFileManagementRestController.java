package uk.co.ogauthority.pwa.controller.appprocessing.casenotes;

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
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;

@RestController
@RequestMapping("/pwa/{pwaId}/case-note/file-management")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.ADD_CASE_NOTE)
public class CaseNoteFileManagementRestController {

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.CASE_NOTES;

  private final PwaApplicationService pwaApplicationService;
  private final FileService fileService;
  private final AppFileManagementService appFileManagementService;
  private final AppFileService appFileService;

  public CaseNoteFileManagementRestController(
      PwaApplicationService pwaApplicationService,
      FileService fileService,
      AppFileManagementService appFileManagementService,
      AppFileService appFileService
  ) {
    super();
    this.pwaApplicationService = pwaApplicationService;
    this.fileService = fileService;
    this.appFileManagementService = appFileManagementService;
    this.appFileService = appFileService;
  }

  @GetMapping("/download/{fileId}")
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer pwaId,
      @PathVariable UUID fileId
  ) {
    var pwaApplication = pwaApplicationService.getApplicationFromId(pwaId);

    var file = fileService.find(fileId)
        .orElseThrow(() -> appFileManagementService.getFileNotFoundException(pwaApplication, fileId));

    appFileManagementService.throwIfFileDoesNotBelongToApplicationOrDocumentType(file, pwaApplication, DOCUMENT_TYPE);

    return fileService.download(file);
  }

  @PostMapping("/delete/{fileId}")
  public FileDeleteResponse delete(
      @PathVariable Integer pwaId,
      @PathVariable UUID fileId
  ) {
    var pwaApplication = pwaApplicationService.getApplicationFromId(pwaId);

    var file = fileService.find(fileId)
        .orElseThrow(() -> appFileManagementService.getFileNotFoundException(pwaApplication, fileId));

    appFileManagementService.throwIfFileDoesNotBelongToApplicationOrDocumentType(file, pwaApplication, DOCUMENT_TYPE);

    var appFile = appFileService.getAppFileByPwaApplicationAndFileId(pwaApplication, String.valueOf(fileId));
    appFileService.processFileDeletion(appFile);

    return fileService.delete(file);
  }
}
