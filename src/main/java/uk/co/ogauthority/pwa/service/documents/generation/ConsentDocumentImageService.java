package uk.co.ogauthority.pwa.service.documents.generation;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.exception.TempFileException;
import uk.co.ogauthority.pwa.service.enums.documents.DocumentImageMethod;


@Service
public class ConsentDocumentImageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsentDocumentImageService.class);

  private final FileService fileService;
  private final DocumentImageMethod imageMethod;

  @Autowired
  public ConsentDocumentImageService(
      FileService fileService,
      @Value("${pwa.document-generation.image-method}") DocumentImageMethod imageMethod
  ) {
    this.fileService = fileService;
    this.imageMethod = imageMethod;
  }

  /**
   * For an UploadedFile, return a string that can be used as an html <img> src value.
   */
  public String convertFileToImageSource(UploadedFile uploadedFile) {
    var stopwatch = Stopwatch.createStarted();

    var imageSource = convertFile(uploadedFile);

    var elapsedMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);

    LOGGER.info("{} completed. Took [{}ms]", imageMethod.getDescriptor(), elapsedMs);

    return imageSource;
  }

  /**
   * For a List of UploadedFile, return a map of each file id to a string that can be used as an html <img> src value.
   */
  public Map<String, String> convertFilesToImageSourceMap(List<UploadedFile> uploadedFiles) {
    var stopwatch = Stopwatch.createStarted();

    Map<String, String> fileIdToImgSrcMap = uploadedFiles.stream()
        .collect(Collectors.toMap(uploadedFile -> String.valueOf(uploadedFile.getId()), this::convertFile));

    var elapsedMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);

    LOGGER.info("{} completed. Took [{}ms]", imageMethod.getDescriptor(), elapsedMs);

    return fileIdToImgSrcMap;
  }

  private String convertFile(UploadedFile uploadedFile) {
    var imageSourceUri = imageMethod == DocumentImageMethod.TEMP_FILE ? createTempFile(uploadedFile) : convertToBase64String(uploadedFile);
    return imageMethod.getUriPrefix() + imageSourceUri;
  }

  /**
   * Creates a temporary file from an UploadedFile object. Consumers must delete the
   * temporary file after they have finished using it.
   */
  private String createTempFile(UploadedFile uploadedFile) {
    var fileData = getFileData(uploadedFile);

    String filename = uploadedFile.getName().replace(" ", "");
    String extension = filename.substring(filename.lastIndexOf("."));

    File tempFile = null;
    try {
      tempFile = File.createTempFile(uploadedFile.getId() + filename, extension);
      Files.copy(fileData.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      if (tempFile != null && !tempFile.delete()) {
        LOGGER.error("Failed to delete temp file");
      }
      throw new TempFileException(String.format(
          "Failed to create temporary file for UploadedFile with ID: [%s]", uploadedFile.getId()), e);
    }

    var path = tempFile.getAbsolutePath();
    return path.replace("\\", "/");
  }

  private String convertToBase64String(UploadedFile uploadedFile) {
    var fileData = getFileData(uploadedFile);

    try {
      if (fileData != null) {
        return Base64.encodeBase64String(fileData.getContentAsByteArray());
      } else {
        throw new RuntimeException("File with ID: [%s] has no content".formatted(uploadedFile.getId()));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private InputStreamResource getFileData(UploadedFile uploadedFile) {
    var downloadResponse = fileService.download(uploadedFile);

    if (downloadResponse.getStatusCode().isError()) {
      LOGGER.warn("Failed to download file");

      throw new ResponseStatusException(
          downloadResponse.getStatusCode(),
          "Failed to download file with ID: [%s]".formatted(uploadedFile.getId())
      );
    }

    var fileData = downloadResponse.getBody();

    if (fileData == null) {
      LOGGER.warn("File download response is null");

      throw new RuntimeException("File download response for ID: [%s] has null body".formatted(uploadedFile.getId()));
    }

    return fileData;
  }

}
