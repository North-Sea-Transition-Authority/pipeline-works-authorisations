package uk.co.ogauthority.pwa.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;

public class FileServiceUtils {

  private FileServiceUtils() {
    throw new AssertionError();
  }

  public static Map<String, UploadFileWithDescriptionForm> getFileIdToFormMap(
      UploadMultipleFilesWithDescriptionForm uploadForm) {
    return uploadForm.getUploadedFileWithDescriptionForms().stream()
        .collect(Collectors.toMap(UploadFileWithDescriptionForm::getUploadedFileId, f -> f));
  }

  public static List<UploadedFileView> getFilesLinkedToForm(UploadMultipleFilesWithDescriptionForm uploadForm,
                                                            List<UploadedFileView> uploadedFileViews) {

    Map<String, UploadFileWithDescriptionForm> fileIdToFormMap = getFileIdToFormMap(uploadForm);

    var formFileViewList = uploadedFileViews.stream()
        .filter(fileView -> fileIdToFormMap.containsKey(fileView.getFileId()))
        .collect(Collectors.toList());

    formFileViewList.forEach(fileView ->
        fileView.setFileDescription(fileIdToFormMap.get(fileView.getFileId()).getUploadedFileDescription()));

    return formFileViewList;

  }

}
