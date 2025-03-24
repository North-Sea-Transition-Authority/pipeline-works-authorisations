package uk.co.ogauthority.pwa.features.filemanagement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;

@ExtendWith(MockitoExtension.class)
class AppFileManagementTestHarnessServiceTest {
  @Mock
  private FileService fileService;

  @Mock
  private AppFileManagementService appFileManagementService;

  @Mock
  private AppFileService appFileService;

  @InjectMocks
  private AppFileManagementTestHarnessService appFileManagementTestHarnessService;

  private FileUploadResponse fileUploadResponse;

  private PwaApplication pwaApplication;

  @BeforeEach
  void setUp() {
    fileUploadResponse = Mockito.mock(FileUploadResponse.class);

    pwaApplication = new PwaApplication();

    when(fileService.upload(any())).thenReturn(fileUploadResponse);
  }

  @Test
  void uploadFileAndMapToForm() {
    var form = new PublicNoticeDraftForm();

    appFileManagementTestHarnessService.uploadFileAndMapToForm(
        form,
        pwaApplication,
        FileDocumentType.PUBLIC_NOTICE,
        AppFilePurpose.PUBLIC_NOTICE
    );

    verify(appFileManagementService).saveFiles(form, pwaApplication, FileDocumentType.PUBLIC_NOTICE);
    verify(appFileService).processInitialUpload(fileUploadResponse, pwaApplication, AppFilePurpose.PUBLIC_NOTICE);
    verify(appFileManagementService).mapFilesToForm(form, pwaApplication, FileDocumentType.PUBLIC_NOTICE);
  }
}