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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationForm;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadFileManagementTestHarnessServiceTest {
  @Mock
  private FileService fileService;

  @Mock
  private PadFileManagementService padFileManagementService;

  @Mock
  private PadFileService padFileService;

  @InjectMocks
  private PadFileManagementTestHarnessService padFileManagementTestHarnessService;

  private FileUploadResponse fileUploadResponse;

  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    fileUploadResponse = Mockito.mock(FileUploadResponse.class);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(fileService.upload(any())).thenReturn(fileUploadResponse);
  }

  @Test
  void uploadFileAndMapToForm() {
    var form = new ProjectInformationForm();

    padFileManagementTestHarnessService.uploadFileAndMapToForm(
        form,
        pwaApplicationDetail,
        FileDocumentType.PROJECT_INFORMATION
    );

    verify(padFileManagementService).saveFiles(form, pwaApplicationDetail, FileDocumentType.PROJECT_INFORMATION);
    verify(padFileManagementService).mapFilesToForm(form, pwaApplicationDetail, FileDocumentType.PROJECT_INFORMATION);
  }

  @Test
  void uploadFileAndMapToFormWithLegacyPadFileLink() {
    var form = new PermanentDepositDrawingForm();

    padFileManagementTestHarnessService.uploadFileAndMapToFormWithLegacyPadFileLink(
        form,
        pwaApplicationDetail,
        FileDocumentType.DEPOSIT_DRAWINGS,
        ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS
    );

    verify(padFileManagementService).saveFiles(form, pwaApplicationDetail, FileDocumentType.DEPOSIT_DRAWINGS);
    verify(padFileService).savePadFileIfValid(fileUploadResponse, pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS);
    verify(padFileManagementService).mapFilesToForm(form, pwaApplicationDetail, FileDocumentType.DEPOSIT_DRAWINGS);
  }
}