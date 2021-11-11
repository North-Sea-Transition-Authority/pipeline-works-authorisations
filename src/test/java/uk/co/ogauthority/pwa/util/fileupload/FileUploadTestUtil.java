package uk.co.ogauthority.pwa.util.fileupload;

import java.time.Instant;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

public class FileUploadTestUtil {

  private static String FILE_ID = "file_305c478a-1b0d-48ed-9564-6e57eaa889cd";
  private static String FILE_DESCRIPTION = "My test file description";

  public static void addUploadFileWithDescriptionOverMaxCharsToForm(UploadMultipleFilesWithDescriptionForm uploadFilesForm) {
    var uploadFileForm = createDefaultUploadFileForm();
    uploadFileForm.setUploadedFileDescription(ValidatorTestUtils.overMaxDefaultCharLength());
    uploadFilesForm.getUploadedFileWithDescriptionForms().add(uploadFileForm);
  }

  public static void addUploadFileWithoutDescriptionToForm(UploadMultipleFilesWithDescriptionForm uploadFilesForm) {
    var uploadFileForm = createDefaultUploadFileForm();
    uploadFileForm.setUploadedFileDescription(null);
    uploadFilesForm.getUploadedFileWithDescriptionForms().add(uploadFileForm);
  }

  public static void addDefaultUploadFileToForm(UploadMultipleFilesWithDescriptionForm uploadFilesForm) {
    uploadFilesForm.getUploadedFileWithDescriptionForms().add(createDefaultUploadFileForm());
  }

  public static String getFirstUploadedFileDescriptionFieldPath() {
    return "uploadedFileWithDescriptionForms[0].uploadedFileDescription";
  }

  public static String getUploadedFileDescriptionFieldPath(int index) {
    return String.format("uploadedFileWithDescriptionForms[%s].uploadedFileDescription", index);
  }


  public static UploadFileWithDescriptionForm createDefaultUploadFileForm() {
    return new UploadFileWithDescriptionForm(FILE_ID, FILE_DESCRIPTION, Instant.now());
  }

}
