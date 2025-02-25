package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.List;
import java.util.Objects;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

public class FileValidationUtils {

  public static final String BELOW_THRESHOLD_ERROR_CODE = "%s.belowThreshold";
  public static final String ABOVE_LIMIT_ERROR_CODE = "%s.limitExceeded";

  private FileValidationUtils() {
    throw new AssertionError();
  }

  public static Validator validator() {
    return new Validator();
  }

  public static class Validator {

    private int maximumNumberOfFiles = Integer.MAX_VALUE;
    private String maxErrorMessage;

    private int minimumNumberOfFiles = 0;
    private String minErrorMessage;
    private boolean isPartialValidator = false;

    private Validator() {
    }

    public Validator withMaximumNumberOfFiles(int maxFileCount, String errorMessage) {
      this.maximumNumberOfFiles = maxFileCount;
      this.maxErrorMessage = errorMessage;
      return this;
    }

    public Validator withMinimumNumberOfFiles(int minFileCount, String errorMessage) {
      this.minimumNumberOfFiles = minFileCount;
      this.minErrorMessage = errorMessage;
      return this;
    }

    public Validator isPartiallyValidated() {
      this.isPartialValidator = true;
      return this;
    }

    public void validate(Errors errors, List<UploadedFileForm> fileUploadForms, String fieldName) {

      if (!isPartialValidator && minimumNumberOfFiles > 0 && CollectionUtils.isEmpty(fileUploadForms)) {
        errors.rejectValue(
            fieldName,
            BELOW_THRESHOLD_ERROR_CODE.formatted(fieldName),
            minErrorMessage
        );
        return;
      }

      if (fileUploadForms != null && fileUploadForms.size() > maximumNumberOfFiles) {
        errors.rejectValue(
            fieldName,
            ABOVE_LIMIT_ERROR_CODE.formatted(fieldName),
            maxErrorMessage
        );
        return;
      }

      if (!isPartialValidator) {
        FileUploadValidationUtils.rejectIfFileDescriptionsAreEmptyOrWhitespace(
            errors,
            Objects.requireNonNull(fileUploadForms),
            fieldName
        );
      }
    }
  }
}
