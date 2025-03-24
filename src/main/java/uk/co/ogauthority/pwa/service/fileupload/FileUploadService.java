package uk.co.ogauthority.pwa.service.fileupload;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.TempFileException;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFileOld;
import uk.co.ogauthority.pwa.repository.files.UploadedFileRepositoryOld;

@Service
public class FileUploadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadService.class);

  private final UploadedFileRepositoryOld uploadedFileRepositoryOld;

  @Autowired
  public FileUploadService(UploadedFileRepositoryOld uploaded) {
    this.uploadedFileRepositoryOld = uploaded;
  }

  public List<UploadedFileOld> getFilesByIds(Collection<String> fileIds) {
    return uploadedFileRepositoryOld.getAllByFileIdIn(fileIds);
  }

  /**
   * Creates a temporary file from an UploadedFile object. Consumers must delete the
   * temporary file after they have finished using it.
   */
  public File createTempFile(UploadedFileOld uploadedFile) {

    String filename = uploadedFile.getFileName().replace(" ", "");
    String extension = filename.substring(filename.lastIndexOf("."));

    File tempFile = null;
    try {
      tempFile = File.createTempFile(uploadedFile.getFileId() + filename, extension);
      var blobToSave = Optional.ofNullable(uploadedFile.getScaledImageData())
          .orElse(uploadedFile.getFileData());
      Files.copy(blobToSave.getBinaryStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return tempFile;
    } catch (Exception e) {
      if (tempFile != null && !tempFile.delete()) {
        LOGGER.error("Failed to delete temp file");
      }
      throw new TempFileException(String.format(
          "Failed to create temporary file for UploadedFile with ID: [%s]", uploadedFile.getFileId()), e);
    }

  }

}
