package uk.co.ogauthority.pwa.features.filemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class AppFileManagementServiceTest {
  @Mock
  private FileService fileService;

  @Mock
  private FileManagementService fileManagementService;

  @InjectMocks
  private AppFileManagementService appFileManagementService;

  private static final String USAGE_TYPE = PwaApplication.class.getSimpleName();
  private static final UUID FILE_ID = UUID.randomUUID();
  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.PROJECT_LAYOUT;
  private static final FileUploadForm FILE_UPLOAD_FORM = new ProjectInformationForm();
  private static final List<UploadedFileForm> EXISTING_FILE_FORMS = Collections.emptyList();
  
  private PwaApplication pwaApplication;

  @BeforeEach
  void setUp() {
    pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
  }
  
  @Test
  void saveDocuments_VerifyMethodCall() {

    appFileManagementService.saveFiles(FILE_UPLOAD_FORM, pwaApplication, DOCUMENT_TYPE);

    verify(fileManagementService).saveFiles(EXISTING_FILE_FORMS, pwaApplication.getId().toString(), USAGE_TYPE, DOCUMENT_TYPE);
  }

  @Test
  void getUploadedFiles() {
    var uploadedFiles = Collections.<UploadedFile>emptyList();
    when(appFileManagementService.getUploadedFiles(pwaApplication, DOCUMENT_TYPE))
        .thenReturn(uploadedFiles);

    assertThat(appFileManagementService.getUploadedFiles(pwaApplication, DOCUMENT_TYPE))
        .isEqualTo(uploadedFiles);
  }

  @Test
  void getFileNotFoundException_VerifyMethodCall() {
    appFileManagementService.getFileNotFoundException(FILE_ID, pwaApplication);

    verify(fileManagementService).getFileNotFoundException(FILE_ID, USAGE_TYPE, pwaApplication.getId().toString());
  }

  @Test
  void throwIfFileDoesNotBelongToSubmissionDetail_VerifyMethodCall() {
    var uploadedFile = new UploadedFile();
    appFileManagementService.throwIfFileDoesNotBelongToApplication(uploadedFile, pwaApplication);

    verify(fileManagementService).throwIfFileDoesNotBelongToUsageType(uploadedFile, pwaApplication.getId().toString(), USAGE_TYPE, null);
  }

  @Test
  void fileUploadComponentAttributes_VerifyMethodCall() {

    var builder = FileUploadComponentAttributes.newBuilder()
        .withMaximumSize(DataSize.ofBytes(1));
    when(fileManagementService.getFileUploadComponentAttributesBuilder(EXISTING_FILE_FORMS, DOCUMENT_TYPE))
        .thenReturn(builder);

    assertThat(appFileManagementService.getFileUploadComponentAttributes(EXISTING_FILE_FORMS, DOCUMENT_TYPE, pwaApplication))
        .extracting(
            FileUploadComponentAttributes::uploadUrl,
            FileUploadComponentAttributes::downloadUrl,
            FileUploadComponentAttributes::deleteUrl
        ).containsExactly(
            ReverseRouter.route(on(FileManagementRestController.class).upload(null)),
            ReverseRouter.route(on(AppFileManagementRestController.class).download(pwaApplication.getId(), null)),
            ReverseRouter.route(on(AppFileManagementRestController.class).delete(pwaApplication.getId(), null))
        );

    verify(fileManagementService).getFileUploadComponentAttributesBuilder(EXISTING_FILE_FORMS, DOCUMENT_TYPE);
  }
}