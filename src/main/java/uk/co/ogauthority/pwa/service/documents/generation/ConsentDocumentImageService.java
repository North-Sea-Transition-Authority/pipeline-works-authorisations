package uk.co.ogauthority.pwa.service.documents.generation;

import com.google.common.base.Stopwatch;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.service.enums.documents.DocumentImageMethod;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;

@Service
public class ConsentDocumentImageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsentDocumentImageService.class);

  private final FileUploadService fileUploadService;

  private final DocumentImageMethod imageMethod;

  @Autowired
  public ConsentDocumentImageService(FileUploadService fileUploadService,
                                     @Value("${pwa.document-generation.image-method}") DocumentImageMethod imageMethod) {
    this.fileUploadService = fileUploadService;
    this.imageMethod = imageMethod;
  }

  /**
   * For a set of file ids, return a map of each file id to a string that can be used as an html <img> src value.
   */
  public Map<String, String> convertFilesToImageSourceMap(Set<String> fileIds) {

    var stopwatch = Stopwatch.createStarted();

    Map<String, String> fileIdToImgSrcMap = fileUploadService.getFilesByIds(fileIds)
        .stream()
        .collect(Collectors.toMap(UploadedFile::getFileId, this::convertFileToImgSource));

    var elapsedMs = stopwatch.elapsed(TimeUnit.MILLISECONDS);

    LOGGER.info("{} completed. Took [{}ms]", imageMethod.getDescriptor(), elapsedMs);

    return fileIdToImgSrcMap;

  }

  private String convertFileToImgSource(UploadedFile uploadedFile) {
    var imageSourceUri = imageMethod == DocumentImageMethod.TEMP_FILE ? createTempFile(uploadedFile) : convertToBase64String(uploadedFile);
    return imageMethod.getUriPrefix() + imageSourceUri;
  }

  private String createTempFile(UploadedFile uploadedFile) {
    var newFile = fileUploadService.createTempFile(uploadedFile);
    var path = newFile.getAbsolutePath();
    return path.replace("\\", "/");
  }

  private String convertToBase64String(UploadedFile file) {

    try {

      Blob blobToConvert = Optional.ofNullable(file.getScaledImageData())
          .orElse(file.getFileData());

      return Base64.encodeBase64String(blobToConvert.getBytes(1, (int) blobToConvert.length()));

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

  }

}
