package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

@RestController
@RequestMapping("/file-management")
class FileManagementRestController {

  private final FileService fileService;

  public FileManagementRestController(FileService fileService) {
    this.fileService = fileService;
  }

  @GetMapping("/download/{fileId}")
  public ResponseEntity<InputStreamResource> download(
      @PathVariable UUID fileId,
      AuthenticatedUserAccount user
  ) {

    return fileService.find(fileId)
        .filter(uploadedFile -> canAccessFile(uploadedFile, user))
        .map(fileService::download)
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @PostMapping("/upload")
  FileUploadResponse upload(MultipartFile file) {
    return fileService.upload(builder -> builder
        .withFileSource(FileSource.fromMultipartFile(file))
        .build());
  }

  @PostMapping("/delete/{fileId}")
  public FileDeleteResponse delete(
      @PathVariable UUID fileId,
      AuthenticatedUserAccount user
  ) {

    return fileService.find(fileId)
        .stream()
        .filter(uploadedFile -> canAccessFile(uploadedFile, user))
        .map(fileService::delete)
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No file found with ID [%s] belonging to user [%d]".formatted(fileId, user.getWuaId())
        ));
  }

  private boolean canAccessFile(
      UploadedFile uploadedFile,
      AuthenticatedUserAccount user
  ) {
    return FileManagementControllerUtils.hasNoUsage(uploadedFile)
        && FileManagementControllerUtils.fileBelongsToUser(uploadedFile, user);
  }
}
