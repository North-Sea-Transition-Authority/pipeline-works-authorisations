package uk.co.ogauthority.pwa.features.filemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
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
  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.PUBLIC_NOTICE;
  private static final FileUploadForm FILE_UPLOAD_FORM = new ProjectInformationForm();
  private static final List<UploadedFileForm> EXISTING_FILE_FORMS = Collections.emptyList();
  
  private PwaApplication pwaApplication;

  @BeforeEach
  void setUp() {
    pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
  }
  
  @Test
  void saveFiles() {
    appFileManagementService.saveFiles(FILE_UPLOAD_FORM, pwaApplication, DOCUMENT_TYPE);

    verify(fileManagementService).saveFiles(EXISTING_FILE_FORMS, pwaApplication.getId().toString(), USAGE_TYPE, DOCUMENT_TYPE);
  }

  @Test
  void getUploadedFile() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setUsageId(pwaApplication.getId().toString());
    uploadedFile.setUsageType(USAGE_TYPE);

    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    assertThat(appFileManagementService.getUploadedFile(pwaApplication, FILE_ID)).isEqualTo(uploadedFile);
  }

  @Test
  void getUploadedFile_fileDoesNotExist() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setUsageId(pwaApplication.getId().toString());
    uploadedFile.setUsageType(USAGE_TYPE);

    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());
    when(fileManagementService.getFileNotFoundException(FILE_ID, USAGE_TYPE, pwaApplication.getId().toString()))
        .thenReturn(new ResponseStatusException(HttpStatusCode.valueOf(404)));

    assertThatThrownBy(() -> appFileManagementService.getUploadedFile(pwaApplication, FILE_ID)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void getUploadedFile_fileDoesNotMatchAppDetail() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setUsageId(pwaApplication.getId().toString());
    uploadedFile.setUsageType(USAGE_TYPE);

    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));
    Mockito.doThrow(new ResponseStatusException(HttpStatusCode.valueOf(404))).when(fileManagementService)
        .throwIfFileDoesNotBelongToUsageType(uploadedFile, pwaApplication.getId().toString(), USAGE_TYPE, null);

    assertThatThrownBy(() -> appFileManagementService.getUploadedFile(pwaApplication, FILE_ID)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void getUploadedFiles() {
    var uploadedFiles = Collections.<UploadedFile>emptyList();
    when(fileService.findAll(pwaApplication.getId().toString(), USAGE_TYPE,  DOCUMENT_TYPE.name()))
        .thenReturn(uploadedFiles);

    assertThat(appFileManagementService.getUploadedFiles(pwaApplication, DOCUMENT_TYPE))
        .isEqualTo(uploadedFiles);
  }

  @Test
  void mapFilesToForm() {
    var form = new ProjectInformationForm();

    appFileManagementService.mapFilesToForm(form, pwaApplication, DOCUMENT_TYPE);

    verify(fileManagementService).mapFilesToForm(form, pwaApplication.getId().toString(), USAGE_TYPE, DOCUMENT_TYPE);
  }

  @Test
  void getFileNotFoundException() {
    appFileManagementService.getFileNotFoundException(pwaApplication, FILE_ID);

    verify(fileManagementService).getFileNotFoundException(FILE_ID, USAGE_TYPE, pwaApplication.getId().toString());
  }

  @Test
  void throwIfFileDoesNotBelongToApplicationOrDocumentType() {
    var uploadedFile = new UploadedFile();
    appFileManagementService.throwIfFileDoesNotBelongToApplicationOrDocumentType(uploadedFile, pwaApplication, DOCUMENT_TYPE);

    verify(fileManagementService).throwIfFileDoesNotBelongToUsageType(uploadedFile, pwaApplication.getId().toString(), USAGE_TYPE, DOCUMENT_TYPE.name());
  }

  @Test
  void getUploadedFileView() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setName("name");
    uploadedFile.setContentLength(50000L);
    uploadedFile.setDescription("description");
    uploadedFile.setUploadedAt(Instant.now());
    uploadedFile.setUsageId(pwaApplication.getId().toString());
    uploadedFile.setUsageType(USAGE_TYPE);

    var uploadedFileView = new UploadedFileView(
        String.valueOf(uploadedFile.getId()),
        uploadedFile.getName(),
        uploadedFile.getContentLength(),
        uploadedFile.getDescription(),
        uploadedFile.getUploadedAt(),
        ReverseRouter.route(on(PadFileManagementRestController.class).download(pwaApplication.getId(), uploadedFile.getId()))
    );

    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    assertThat(appFileManagementService.getUploadedFileView(pwaApplication, FILE_ID)).isEqualTo(uploadedFileView);
  }

  @Test
  void getUploadedFileViews() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setName("name");
    uploadedFile.setContentLength(50000L);
    uploadedFile.setDescription("description");
    uploadedFile.setUploadedAt(Instant.now());
    uploadedFile.setUsageId(pwaApplication.getId().toString());
    uploadedFile.setUsageType(USAGE_TYPE);

    var uploadedFileView = new UploadedFileView(
        String.valueOf(uploadedFile.getId()),
        uploadedFile.getName(),
        uploadedFile.getContentLength(),
        uploadedFile.getDescription(),
        uploadedFile.getUploadedAt(),
        ReverseRouter.route(on(PadFileManagementRestController.class).download(pwaApplication.getId(), uploadedFile.getId()))
    );

    when(fileService.findAll(pwaApplication.getId().toString(), USAGE_TYPE, DOCUMENT_TYPE.name())).thenReturn(List.of(uploadedFile));

    assertThat(appFileManagementService.getUploadedFileViews(pwaApplication, DOCUMENT_TYPE)).isEqualTo(List.of(uploadedFileView));
  }
}