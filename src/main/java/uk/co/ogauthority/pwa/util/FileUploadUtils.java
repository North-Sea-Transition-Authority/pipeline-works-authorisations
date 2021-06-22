package uk.co.ogauthority.pwa.util;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

public class FileUploadUtils {

  private FileUploadUtils() {
    throw new AssertionError();
  }

  public static void validateMaxFileLimit(UploadMultipleFilesWithDescriptionForm uploadForm,
                                          Errors errors,
                                          int maxFileCount,
                                          String limitExceededMessage) {

    if (uploadForm.getUploadedFileWithDescriptionForms().size() > maxFileCount) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode(),
          limitExceededMessage);
    }

  }

  public static void validateMaxFileLimitWithFileId(UploadMultipleFilesWithDescriptionForm uploadForm,
                                          Errors errors,
                                          int maxFileCount,
                                          String limitExceededMessage) {

    if (uploadForm.getFileFormsForValidation().size() > maxFileCount) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode(),
          limitExceededMessage);
    }

  }

  public static void updateFormToExcludeNullFiles(UploadMultipleFilesWithDescriptionForm uploadForm) {
    uploadForm.setUploadedFileWithDescriptionForms(uploadForm.getFileFormsForValidation());
  }

  public static void validateMinFileLimit(UploadMultipleFilesWithDescriptionForm uploadForm,
                                          Errors errors,
                                          int minFileLimit,
                                          String limitNotReachedMessage) {

    if (uploadForm.getFileFormsForValidation().size() < minFileLimit) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.errorCode("uploadedFileWithDescriptionForms"),
          limitNotReachedMessage);
    }

  }

  public static void validateFiles(UploadMultipleFilesWithDescriptionForm uploadForm,
                                   Errors errors,
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
              errors,
              String.format("%s.uploadedFileDescription", form),
              FieldValidationErrorCodes.REQUIRED.errorCode(String.format("%s.uploadedFileDescription", form)),
              "File must have a description"
          );

        });

    if (mandatory) {
      validateMinFileLimit(uploadForm, errors, 1, "Upload at least one file");
    }

  }

}
