package uk.co.ogauthority.pwa.service.fileupload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.file.AppFileRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppFileServiceTest {

  private final String FILE_ID = String.valueOf(UUID.randomUUID());

  @Mock
  private AppFileRepository appFileRepository;

  @Captor
  private ArgumentCaptor<AppFile> appFileCaptor;

  private AppFileService appFileService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaApplication application;

  private AppFile file;

  @BeforeEach
  void setUp() {

    appFileService = new AppFileService(appFileRepository);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    application = pwaApplicationDetail.getPwaApplication();
    file = new AppFile();
    file.setFileId(FILE_ID);
    file.setPurpose(AppFilePurpose.CASE_NOTES);

    when(appFileRepository.findAllByPwaApplicationAndPurpose(application, AppFilePurpose.CASE_NOTES))
        .thenReturn(List.of(file));

  }

  @Test
  void processInitialUpload_success() {
    var response = FileUploadResponse.success(
        UUID.fromString(file.getFileId()),
        FileSource.fromMultipartFile(new MockMultipartFile("test", new byte[]{}))
    );

    appFileService.processInitialUpload(response, application, AppFilePurpose.CASE_NOTES);

    verify(appFileRepository, times(1)).save(appFileCaptor.capture());

    var newFile = appFileCaptor.getValue();

    assertThat(newFile.getPwaApplication()).isEqualTo(application);
    assertThat(newFile.getFileId()).isEqualTo(file.getFileId());
    assertThat(newFile.getDescription()).isNull();
    assertThat(newFile.getPurpose()).isEqualTo(AppFilePurpose.CASE_NOTES);
    assertThat(newFile.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.FULL);
  }

  @Test
  void processInitialUpload_failed() {
    var response = FileUploadResponse.error(
        FileSource.fromMultipartFile(new MockMultipartFile("test", new byte[]{})),
        ""
    );

    appFileService.processInitialUpload(response, application, AppFilePurpose.CASE_NOTES);

    verify(appFileRepository, never()).save(any());
  }

  @Test
  void processFileDeletion_verifyServiceInteractions() {
    appFileService.processFileDeletion(file);

    verify(appFileRepository, times(1)).delete(file);
  }

}