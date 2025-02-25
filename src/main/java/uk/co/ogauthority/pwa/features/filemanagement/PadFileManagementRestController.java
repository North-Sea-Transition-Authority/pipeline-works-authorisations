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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@RestController
@RequestMapping("/pwa-application/{applicationId}/file-management")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.HUOO_VARIATION
})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
class PadFileManagementRestController {

  private final FileService fileService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadFileManagementService padFileManagementService;

  PadFileManagementRestController(
      FileService fileService,
      PwaApplicationDetailService pwaApplicationDetailService,
      PadFileManagementService padFileManagementService
  ) {
    this.fileService = fileService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.padFileManagementService = padFileManagementService;
  }

  @GetMapping("/download/{fileId}")
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW})
  ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId
  ) {
    var pwaApplicationDetail = pwaApplicationDetailService.getDetailByDetailId(applicationId);

    var file = fileService.find(fileId)
        .orElseThrow(() -> padFileManagementService.getFileNotFoundException(fileId, pwaApplicationDetail));

    padFileManagementService.throwIfFileDoesNotBelongToApplicationDetail(file, pwaApplicationDetail);

    return fileService.download(file);
  }

  @PostMapping("/delete/{fileId}")
  //@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  FileDeleteResponse delete(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId
  ) {
    var pwaApplicationDetail = pwaApplicationDetailService.getDetailByDetailId(applicationId);

    var file = fileService.find(fileId)
        .orElseThrow(() -> padFileManagementService.getFileNotFoundException(fileId, pwaApplicationDetail));

    padFileManagementService.throwIfFileDoesNotBelongToApplicationDetail(file, pwaApplicationDetail);

    return fileService.delete(file);
  }
}