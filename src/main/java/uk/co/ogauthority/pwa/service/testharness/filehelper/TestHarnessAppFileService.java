package uk.co.ogauthority.pwa.service.testharness.filehelper;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.exception.TempFileException;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;

@Service
@Profile("test-harness")
public class TestHarnessAppFileService {

  private final AppFileService appFileService;


  @Autowired
  public TestHarnessAppFileService(AppFileService appFileService) {
    this.appFileService = appFileService;
  }



  public String generateInitialUpload(WebUserAccount user, PwaApplication pwaApplication, AppFilePurpose filePurpose) {

    var multipartFile = FileUploadTestHarnessUtil.getSampleMultipartFile();
    var fileUploadResult = appFileService.processInitialUpload(
        multipartFile,
        pwaApplication,
        filePurpose,
        user);

    return getFileIdFromFileUploadResult(fileUploadResult);
  }

  private String getFileIdFromFileUploadResult(FileUploadResult fileUploadResult) {
    return fileUploadResult.getFileId().orElseThrow(() ->
        new TempFileException("Error getting file id from temporary uploaded file"));
  }

  public void updateAppFiles(UploadMultipleFilesWithDescriptionForm form,
                             WebUserAccount user,
                             PwaApplication pwaApplication,
                             AppFilePurpose filePurpose,
                             FileUpdateMode updateMode) {
    appFileService.updateFiles(
        form, pwaApplication, filePurpose, updateMode, user);
  }

  public void setFileIdOnForm(String fileId, List<UploadFileWithDescriptionForm> forms) {
    var uploadFileForm = new UploadFileWithDescriptionForm(fileId, FileUploadTestHarnessUtil.getFileDescription(), Instant.now());
    forms.add(uploadFileForm);
  }




}
