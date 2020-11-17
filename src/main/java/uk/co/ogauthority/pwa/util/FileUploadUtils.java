package uk.co.ogauthority.pwa.util;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

public class FileUploadUtils {

  public FileUploadUtils() {
    throw new AssertionError();
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

  public static void validateMinFileLimit(UploadMultipleFilesWithDescriptionForm uploadForm,
                                          BindingResult bindingResult,
                                          int minFileLimit,
                                          String limitNotReachedMessage) {

    if (uploadForm.getFileFormsForValidation().size() < minFileLimit) {
      bindingResult.rejectValue("uploadedFileWithDescriptionForms",
          FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.errorCode("uploadedFileWithDescriptionForms"),
          limitNotReachedMessage);
    }

  }

  public static void validateFiles(UploadMultipleFilesWithDescriptionForm uploadForm,
                                   BindingResult bindingResult,
                                   List<Object> hints) {

    boolean mandatory = hints.stream()
        .anyMatch(h -> h == MandatoryUploadValidation.class);

    var fileFormsToValidate = uploadForm.getFileFormsForValidation();

    // filter out pre-existing files that have been deleted so that we don't get unexpected errors
    IntStream.range(0, uploadForm.getUploadedFileWithDescriptionForms().size())
        .filter(i -> fileFormsToValidate.contains(uploadForm.getUploadedFileWithDescriptionForms().get(i)))
        .forEach(i -> {

          String form = "uploadedFileWithDescriptionForms[" + i + "]";

          ValidationUtils.rejectIfEmpty(
              bindingResult,
              String.format("%s.uploadedFileDescription", form),
              FieldValidationErrorCodes.REQUIRED.errorCode(String.format("%s.uploadedFileDescription", form)),
              "File must have a description"
          );

        });

    if (mandatory) {
      validateMinFileLimit(uploadForm, bindingResult, 1, "Upload at least one file");
    }

  }

}
