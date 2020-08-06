package uk.co.ogauthority.pwa.util;

import java.util.Optional;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;

public class FileUploadUtils {

  public FileUploadUtils() {
    throw new AssertionError();
  }

  public static Optional<String> getDropzoneErrorMessage(BindingResult bindingResult) {
    return bindingResult.getFieldErrors("uploadedFileWithDescriptionForms")
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .findFirst();
  }

}
