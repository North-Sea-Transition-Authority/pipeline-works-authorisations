package uk.co.ogauthority.pwa.util;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

public class FileUploadUtils {

  private FileUploadUtils() {
    throw new AssertionError();
  }

  public static final String UPLOADED_FILE_FIELD_NAME = "uploadedFileWithDescriptionForms";
  public static final String UPLOADED_FILE_ERROR_ELEMENT_ID = UPLOADED_FILE_FIELD_NAME + "-error";

  public static void validateMaxFileLimit(UploadMultipleFilesWithDescriptionForm uploadForm,
                                          Errors errors,
                                          int maxFileCount,
                                          String limitExceededMessage) {

    if (uploadForm.getFileFormsForValidation().size() > maxFileCount) {

      errors.rejectValue(UPLOADED_FILE_FIELD_NAME,
          UPLOADED_FILE_FIELD_NAME + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode(),
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
      errors.rejectValue(UPLOADED_FILE_FIELD_NAME,
          FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.errorCode(UPLOADED_FILE_FIELD_NAME),
          limitNotReachedMessage);
    }

  }

  public static void validateFiles(UploadMultipleFilesWithDescriptionForm uploadForm,
                                   Errors errors,
                                   List<Object> hints,
                                   String limitNotReachedMessage) {

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
      validateMinFileLimit(uploadForm, errors, 1, limitNotReachedMessage);
    }

  }

  public static void validateFiles(UploadMultipleFilesWithDescriptionForm uploadForm,
                                   Errors errors,
                                   List<Object> hints) {

    validateFiles(uploadForm, errors, hints, "Upload at least one file");
  }

}
