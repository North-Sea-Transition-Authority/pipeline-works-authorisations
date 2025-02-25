package uk.co.ogauthority.pwa.features.filemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationForm;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadFileManagementServiceTest {
  @Mock
  private FileService fileService;

  @Mock
  private FileManagementService fileManagementService;

  @InjectMocks
  private PadFileManagementService padFileManagementService;

  private static final String USAGE_TYPE = PwaApplicationDetail.class.getSimpleName();
  private static final UUID FILE_ID = UUID.randomUUID();
  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.PROJECT_LAYOUT;
  private static final PwaApplicationDetail APPLICATION_DETAIL = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private static final FileUploadForm FILE_UPLOAD_FORM = new ProjectInformationForm();
  private static final List<UploadedFileForm> EXISTING_FILE_FORMS = Collections.emptyList();

  @Test
  void saveFiles_VerifyMethodCall() {

    padFileManagementService.saveFiles(FILE_UPLOAD_FORM, APPLICATION_DETAIL, DOCUMENT_TYPE);

    verify(fileManagementService).saveFiles(EXISTING_FILE_FORMS, APPLICATION_DETAIL.getId().toString(), USAGE_TYPE, DOCUMENT_TYPE);
  }

  @Test
  void getUploadedFiles() {
    var uploadedFiles = Collections.<UploadedFile>emptyList();
    when(padFileManagementService.getUploadedFiles(APPLICATION_DETAIL, DOCUMENT_TYPE))
        .thenReturn(uploadedFiles);

    assertThat(padFileManagementService.getUploadedFiles(APPLICATION_DETAIL, DOCUMENT_TYPE))
        .isEqualTo(uploadedFiles);
  }

  @Test
  void getFileNotFoundException_VerifyMethodCall() {
    padFileManagementService.getFileNotFoundException(FILE_ID, APPLICATION_DETAIL);

    verify(fileManagementService).getFileNotFoundException(FILE_ID, USAGE_TYPE, APPLICATION_DETAIL.getId().toString());
  }

  @Test
  void throwIfFileDoesNotBelongToSubmissionDetail_VerifyMethodCall() {
    var uploadedFile = new UploadedFile();
    padFileManagementService.throwIfFileDoesNotBelongToApplicationDetail(uploadedFile, APPLICATION_DETAIL);

    verify(fileManagementService).throwIfFileDoesNotBelongToUsageType(uploadedFile, APPLICATION_DETAIL.getId().toString(), USAGE_TYPE, null);
  }

  @Test
  void fileUploadComponentAttributes_VerifyMethodCall() {

    var builder = FileUploadComponentAttributes.newBuilder()
        .withMaximumSize(DataSize.ofBytes(1));
    when(fileManagementService.getFileUploadComponentAttributesBuilder(EXISTING_FILE_FORMS, DOCUMENT_TYPE))
        .thenReturn(builder);

    assertThat(padFileManagementService.getFileUploadComponentAttributes(EXISTING_FILE_FORMS, DOCUMENT_TYPE, APPLICATION_DETAIL))
        .extracting(
            FileUploadComponentAttributes::uploadUrl,
            FileUploadComponentAttributes::downloadUrl,
            FileUploadComponentAttributes::deleteUrl
        ).containsExactly(
            ReverseRouter.route(on(FileManagementRestController.class).upload(null)),
            ReverseRouter.route(on(PadFileManagementRestController.class).download(APPLICATION_DETAIL.getId(), null)),
            ReverseRouter.route(on(PadFileManagementRestController.class).delete(APPLICATION_DETAIL.getId(), null))
        );

    verify(fileManagementService).getFileUploadComponentAttributesBuilder(EXISTING_FILE_FORMS, DOCUMENT_TYPE);
  }

  @Test
  void copyUploadedFiles() {

    var newApplicationDetail = new PwaApplicationDetail();
    var uploadedFile1 = mock(UploadedFile.class);
    var uploadedFile2 = mock(UploadedFile.class);
    var uploadedFiles = List.of(uploadedFile1, uploadedFile2);

    when(padFileManagementService.getUploadedFiles(APPLICATION_DETAIL, DOCUMENT_TYPE)).thenReturn(uploadedFiles);

    padFileManagementService.copyUploadedFiles(APPLICATION_DETAIL, newApplicationDetail, DOCUMENT_TYPE);

    // Verify that the expected methods were called
    verify(fileService, times(1)).copy(eq(uploadedFile1), any());
    verify(fileService, times(1)).copy(eq(uploadedFile2), any());
  }
}