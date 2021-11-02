package uk.co.ogauthority.pwa.service.testharness.filehelper;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.exception.TempFileException;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;

@Service
@Profile("test-harness")
public class TestHarnessPadFileService {

  private final PadFileService padFileService;


  @Autowired
  public TestHarnessPadFileService(PadFileService padFileService) {
    this.padFileService = padFileService;
  }



  public String generateImageUpload(WebUserAccount user,
                                  PwaApplicationDetail pwaApplicationDetail,
                                  ApplicationDetailFilePurpose filePurpose) {

    var multipartFile = FileUploadTestHarnessUtil.getSampleMultipartFile();
    var fileUploadResult = padFileService.processImageUpload(
        multipartFile,
        pwaApplicationDetail,
        filePurpose,
        user);

    return getFileIdFromFileUploadResult(fileUploadResult);
  }

  public String generateInitialUpload(WebUserAccount user,
                                    PwaApplicationDetail pwaApplicationDetail,
                                    ApplicationDetailFilePurpose filePurpose) {

    var multipartFile = FileUploadTestHarnessUtil.getSampleMultipartFile();
    var fileUploadResult = padFileService.processInitialUpload(
        multipartFile,
        pwaApplicationDetail,
        filePurpose,
        user);

    return getFileIdFromFileUploadResult(fileUploadResult);
  }

  private String getFileIdFromFileUploadResult(FileUploadResult fileUploadResult) {
    return fileUploadResult.getFileId().orElseThrow(() ->
        new TempFileException("Error getting file id from temporary uploaded file"));
  }

  public void updatePadFiles(UploadMultipleFilesWithDescriptionForm form,
                             WebUserAccount user,
                             PwaApplicationDetail pwaApplicationDetail,
                             ApplicationDetailFilePurpose filePurpose,
                             FileUpdateMode updateMode) {
    padFileService.updateFiles(
        form, pwaApplicationDetail, filePurpose, updateMode, user);
  }

  public void setFileIdOnForm(String fileId, List<UploadFileWithDescriptionForm> forms) {
    var uploadFileForm = new UploadFileWithDescriptionForm(fileId, FileUploadTestHarnessUtil.getFileDescription(), Instant.now());
    forms.add(uploadFileForm);
  }




}
