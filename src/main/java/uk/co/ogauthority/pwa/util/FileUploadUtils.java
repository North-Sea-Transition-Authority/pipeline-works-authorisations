package uk.co.ogauthority.pwa.util;

import java.util.Optional;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

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

  public static void validateMaxFileLimit(UploadMultipleFilesWithDescriptionForm uploadForm,
                                          BindingResult bindingResult,
                                          int maxFileCount,
                                          String limitExceededMessage) {

    if (uploadForm.getUploadedFileWithDescriptionForms().size() > maxFileCount) {
      bindingResult.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode(),
          limitExceededMessage);
    }

  }
}
