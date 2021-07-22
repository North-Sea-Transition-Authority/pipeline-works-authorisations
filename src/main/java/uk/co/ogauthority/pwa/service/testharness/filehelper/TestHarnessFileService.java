package uk.co.ogauthority.pwa.service.testharness.filehelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.TempFileException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.file.PadFileRepository;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;

@Service
@Profile("development")
public class TestHarnessFileService {

  private final PadFileService padFileService;
  private final PadFileRepository padFileRepository;


  @Autowired
  public TestHarnessFileService(PadFileService padFileService,
                                PadFileRepository padFileRepository) {
    this.padFileService = padFileService;
    this.padFileRepository = padFileRepository;
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
    return finaliseAndSavePadFile(fileUploadResult, pwaApplicationDetail);
  }

  public void generateInitialUpload(WebUserAccount user,
                                    PwaApplicationDetail pwaApplicationDetail,
                                    ApplicationDetailFilePurpose filePurpose) {

    var multipartFile = FileUploadTestHarnessUtil.getSampleMultipartFile();
    var fileUploadResult = padFileService.processInitialUpload(
        multipartFile,
        pwaApplicationDetail,
        filePurpose,
        user);
    finaliseAndSavePadFile(fileUploadResult, pwaApplicationDetail);
  }


  private String finaliseAndSavePadFile(FileUploadResult fileUploadResult,
                                      PwaApplicationDetail pwaApplicationDetail) {
    var fileId = fileUploadResult.getFileId().orElseThrow(() ->
        new TempFileException("Error getting file id from temporary uploaded file"));
    var padFile = padFileService.getPadFileByPwaApplicationDetailAndFileId(pwaApplicationDetail, fileId);
    padFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);
    padFile.setDescription("test harness app file");
    padFileRepository.save(padFile);
    return fileId;
  }




}
