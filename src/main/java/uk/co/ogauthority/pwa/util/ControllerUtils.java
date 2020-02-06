package uk.co.ogauthority.pwa.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.co.ogauthority.pwa.model.Checkable;

public class ControllerUtils {

  public static ResponseEntity serveResource(Resource resource, long fileSize, String fileName) {
    try {
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .contentLength(fileSize)
          .header(
              HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", fileName))
          .body(resource);
    } catch (Exception e) {
      throw new RuntimeException(String.format("Error serving file '%s'", fileName), e);
    }
  }

  public static Map<String, String> asCheckboxMap(List<? extends Checkable> items) {
    return items.stream()
        .collect(Collectors.toMap(Checkable::getIdentifier, Checkable::getDisplayName));
  }
}
