package uk.co.ogauthority.pwa.service.fileupload;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.BiConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.config.fileupload.UploadErrorType;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.PwaApplicationFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationFileServiceTest {

  private static final String FILE_ID = "file1234567898765432";
  private static final String FILE_NAME = "FILENAME";
  private static final String FILE_CONTENT = "txt";
  private static final long FILE_SIZE = 100L;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private MultipartFile multiPartFile;

  @Spy
  private BiConsumer<String, PwaApplicationDetail> fileIdAndPwaAppDetailConsumer;

  @Mock
  private PwaApplicationFile pwaApplicationFile;

  private PwaApplicationDetail pwaApplicationDetail;

  private WebUserAccount user = new WebUserAccount(1);

  private PwaApplicationFileService pwaApplicationFileService;

  @Before
  public void setUp() throws Exception {
    pwaApplicationFileService = new PwaApplicationFileService(fileUploadService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void processApplicationFileUpload_whenUploadSuccessful() {
    when(fileUploadService.processUpload(multiPartFile, user)).thenReturn(
        FileUploadResult.generateSuccessfulFileUploadResult(
            FILE_ID, FILE_NAME, FILE_SIZE, FILE_CONTENT
        )
    );
    pwaApplicationFileService.processApplicationFileUpload(multiPartFile, user, pwaApplicationDetail,
        fileIdAndPwaAppDetailConsumer);

    verify(fileIdAndPwaAppDetailConsumer, times(1)).accept(FILE_ID, pwaApplicationDetail);
  }

  @Test
  public void processApplicationFileUpload_whenUploadFailed() {
    var result = FileUploadResult.generateFailedFileUploadResult(
        FILE_NAME, FILE_SIZE, FILE_CONTENT, UploadErrorType.INTERNAL_SERVER_ERROR
    );

    when(fileUploadService.processUpload(multiPartFile, user)).thenReturn(result

    );
    pwaApplicationFileService.processApplicationFileUpload(
        multiPartFile,
        user,
        pwaApplicationDetail,
        fileIdAndPwaAppDetailConsumer
    );

    verifyNoInteractions(fileIdAndPwaAppDetailConsumer);
  }

  @Test
  public void processApplicationFileDelete_whenDeleteSuccessful() {

    when(fileUploadService.deleteUploadedFile(FILE_ID, user)).thenReturn(
        FileDeleteResult.generateSuccessfulFileDeleteResult(FILE_ID)
    );

    pwaApplicationFileService.processApplicationFileDelete(
        FILE_ID,
        pwaApplicationDetail,
        user,
        fileIdAndPwaAppDetailConsumer
    );

    verify(fileIdAndPwaAppDetailConsumer, times(1)).accept(FILE_ID, pwaApplicationDetail);

  }

  @Test
  public void processApplicationFileDelete_whenDeleteFailed() {

    when(fileUploadService.deleteUploadedFile(FILE_ID, user)).thenReturn(
        FileDeleteResult.generateFailedFileDeleteResult(FILE_ID)
    );

    pwaApplicationFileService.processApplicationFileDelete(
        FILE_ID,
        pwaApplicationDetail,
        user,
        fileIdAndPwaAppDetailConsumer
    );

    verifyNoInteractions(fileIdAndPwaAppDetailConsumer);

  }

  @Test
  public void getUploadedFile_verifyServiceInteractions() {
    when(pwaApplicationFile.getFileId()).thenReturn(FILE_ID);
    pwaApplicationFileService.getUploadedFile(pwaApplicationFile);
    verify(fileUploadService, times(1)).getFileById(FILE_ID);
  }
}