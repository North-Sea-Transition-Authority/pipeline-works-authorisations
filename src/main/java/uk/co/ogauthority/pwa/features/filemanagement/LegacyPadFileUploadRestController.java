package uk.co.ogauthority.pwa.features.filemanagement;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@RestController
@RequestMapping("/pwa-application/{applicationId}/pad-file-upload/{legacyFilePurpose}")
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
public class LegacyPadFileUploadRestController {

  private final FileService fileService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadFileService padFileService;

  LegacyPadFileUploadRestController(
      FileService fileService,
      PwaApplicationDetailService pwaApplicationDetailService,
      PadFileService padFileService
  ) {
    this.fileService = fileService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.padFileService = padFileService;
  }

  @PostMapping
  FileUploadResponse upload(
      @PathVariable Integer applicationId,
      @PathVariable String legacyFilePurpose,
      MultipartFile file
  ) {
    var purpose = ApplicationDetailFilePurpose.valueOf(legacyFilePurpose);

    var pwaApplicationDetail = pwaApplicationDetailService.getDetailByDetailId(applicationId);

    var response = fileService.upload(builder -> builder
        .withFileSource(FileSource.fromMultipartFile(file))
        .build());

    padFileService.savePadFileIfValid(response, pwaApplicationDetail, purpose);

    return response;
  }
}