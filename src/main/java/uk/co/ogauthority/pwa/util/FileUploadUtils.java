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

  public static void validateFileUploaded(UploadMultipleFilesWithDescriptionForm uploadForm,
                                          Errors errors,
                                          boolean doValidate,
                                          String fileNotUploadedMessage) {

    if (uploadForm.getFileFormsForValidation().size() < 1 & doValidate) {
      errors.rejectValue(UPLOADED_FILE_FIELD_NAME,
          FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.errorCode(UPLOADED_FILE_FIELD_NAME),
          fileNotUploadedMessage);
    }

  }

  private static String getFileFormPathByIndex(int uploadedFileFormIndex) {
    return "uploadedFileWithDescriptionForms[" + uploadedFileFormIndex + "]";
  }

  private static void validateFileDescriptionLength(String fileFormPath,
                                                    String description,
                                                    Errors errors) {
    ValidatorUtils.validateDefaultStringLength(
        errors,
        String.format("%s.uploadedFileDescription", fileFormPath),
        () -> description,
        "File description");
  }

  public static void validateFilesDescriptionLength(UploadMultipleFilesWithDescriptionForm uploadForm,
                                                    Errors errors) {

    var fileFormsToValidate = uploadForm.getFileFormsForValidation();
    IntStream.range(0, uploadForm.getUploadedFileWithDescriptionForms().size())
        .filter(i -> fileFormsToValidate.contains(uploadForm.getUploadedFileWithDescriptionForms().get(i)))
        .forEach(i -> {
          validateFileDescriptionLength(
              getFileFormPathByIndex(i), uploadForm.getUploadedFileWithDescriptionForms().get(i).getUploadedFileDescription(), errors);
        });

  }

  public static void validateFiles(UploadMultipleFilesWithDescriptionForm uploadForm,
                                   Errors errors,
                                   List<Object> hints,
                                   String fileNotUploadedMessage) {

    boolean mandatory = hints.stream()
        .anyMatch(h -> h == MandatoryUploadValidation.class);

    var fileFormsToValidate = uploadForm.getFileFormsForValidation();

    // filter out pre-existing files that have been deleted so that we don't get unexpected errors
    IntStream.range(0, uploadForm.getUploadedFileWithDescriptionForms().size())
        .filter(i -> fileFormsToValidate.contains(uploadForm.getUploadedFileWithDescriptionForms().get(i)))
        .forEach(i -> {

          String form = getFileFormPathByIndex(i);

          ValidationUtils.rejectIfEmpty(
              errors,
              String.format("%s.uploadedFileDescription", form),
              FieldValidationErrorCodes.REQUIRED.errorCode(String.format("%s.uploadedFileDescription", form)),
              "File must have a description"
          );

          validateFileDescriptionLength(
              form, uploadForm.getUploadedFileWithDescriptionForms().get(i).getUploadedFileDescription(), errors);
        });

    if (mandatory) {
      validateFileUploaded(uploadForm, errors, true, fileNotUploadedMessage);
    }

  }

  public static void validateFiles(UploadMultipleFilesWithDescriptionForm uploadForm,
                                   Errors errors,
                                   List<Object> hints) {

    validateFiles(uploadForm, errors, hints, "Upload at least one file");
  }

}
