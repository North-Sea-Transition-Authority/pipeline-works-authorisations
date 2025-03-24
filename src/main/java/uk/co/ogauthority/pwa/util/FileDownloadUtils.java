package uk.co.ogauthority.pwa.util;

import java.io.InputStream;
import java.sql.Blob;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class FileDownloadUtils {

  private FileDownloadUtils() {
  }

  /**
   * Creates the ResponseEntity object to fully configure the HTTP response to the download request.
   *
   * @param resource      the object being downloaded
   * @param mediaType     the content type of the file
   * @param filename      the name of the file
   * @return the ResponseEntity object associated to the required resource
   */
  public static <T> ResponseEntity<T> getCustomMediaTypeObjectAsResponse(T resource,
                                                               String mediaType,
                                                               String filename) {
    return ResponseEntity.status(HttpStatus.OK)
        .header(HttpHeaders.CONTENT_TYPE, mediaType)
        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", filename))
        .body(resource);
  }

  /**
   * Converts the displayed file size into a more readable string with the appropriate unit. Eg. 29573 bytes -> 29MB
   *
   * @param fileSize the size of the file in bytes
   * @return a String specifying the size of the file with the corresponding byte unit
   */
  public static String fileSizeFormatter(Long fileSize) {
    int i = -1;
    String[] byteUnits = {" kB", " MB", " GB", " TB"};
    do {
      fileSize = fileSize / 1024;
      i++;
    } while (fileSize > 1024);

    return Math.max(fileSize, 1) + byteUnits[i];
  }

  public static ResponseEntity<Resource> getResourceResponseEntity(Blob blob, InputStream inputStream, String filename) {

    try {
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .contentLength(blob.length())
          .header(HttpHeaders.CONTENT_DISPOSITION,
              String.format("attachment; filename=\"%s\"", filename))
          .body(new InputStreamResource(inputStream));

    } catch (Exception e) {
      throw new RuntimeException(String.format("Error serving file '%s'", filename), e);
    }

  }

}
