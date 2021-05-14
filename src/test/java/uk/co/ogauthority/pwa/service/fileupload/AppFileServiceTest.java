package uk.co.ogauthority.pwa.service.fileupload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.config.fileupload.UploadErrorType;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.file.AppFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class AppFileServiceTest {

  private final String FILE_ID = "1234567890qwertyuiop";

  @Mock
  private AppFileRepository appFileRepository;

  @Mock
  private FileUploadService fileUploadService;

  @Captor
  private ArgumentCaptor<AppFile> appFileCaptor;

  @Captor
  private ArgumentCaptor<Set<AppFile>> appFileSetCaptor;

  private AppFileService appFileService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaApplication application;

  private AppFile file;

  private WebUserAccount wua = new WebUserAccount(1);

  private UploadedFileView fileView = new UploadedFileView(
      FILE_ID,
      "NAME",
      100L,
      "DESC",
      Instant.now(),
      "");

  @Before
  public void setUp() {

    appFileService = new AppFileService(fileUploadService, appFileRepository);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    application = pwaApplicationDetail.getPwaApplication();
    file = new AppFile();
    file.setFileId(FILE_ID);
    file.setPurpose(AppFilePurpose.CASE_NOTES);

    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateSuccessfulFileDeleteResult(invocation.getArgument(0))
    );

    when(appFileRepository.findAllByPwaApplicationAndPurpose(application, AppFilePurpose.CASE_NOTES))
        .thenReturn(List.of(file));

    when(fileUploadService.createUploadFileWithDescriptionFormFromView(any())).thenCallRealMethod();

  }

  @Test
  public void mapFilesToForm() {

    var form = new AddCaseNoteForm();

    when(appFileRepository.findAllAsFileViewByAppAndPurposeAndFileLinkStatus(
        application, AppFilePurpose.CASE_NOTES, ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    appFileService.mapFilesToForm(form, application, AppFilePurpose.CASE_NOTES);

    assertThat(form.getUploadedFileWithDescriptionForms().size()).isEqualTo(1);

    var uploadForm = form.getUploadedFileWithDescriptionForms().get(0);

    assertThat(uploadForm.getUploadedFileId()).isEqualTo(fileView.getFileId());
    assertThat(uploadForm.getUploadedFileDescription()).isEqualTo(fileView.getFileDescription());
    assertThat(uploadForm.getUploadedFileInstant()).isEqualTo(fileView.getFileUploadedTime());

  }

  @Test
  public void processInitialUpload_success() {

    var multiPartFile = mock(MultipartFile.class);

    when(fileUploadService.processUpload(multiPartFile, wua)).thenReturn(FileUploadResult.generateSuccessfulFileUploadResult(
        file.getFileId(), fileView.getFileName(), 0, "content"));

    var fileUploadResult = appFileService.processInitialUpload(multiPartFile, application, AppFilePurpose.CASE_NOTES, wua);

    assertThat(fileUploadResult.isValid()).isTrue();

    verify(fileUploadService, times(1)).processUpload(multiPartFile, wua);
    verify(appFileRepository, times(1)).save(appFileCaptor.capture());

    var newFile = appFileCaptor.getValue();

    assertThat(newFile.getPwaApplication()).isEqualTo(application);
    assertThat(newFile.getFileId()).isEqualTo(file.getFileId());
    assertThat(newFile.getDescription()).isNull();
    assertThat(newFile.getPurpose()).isEqualTo(AppFilePurpose.CASE_NOTES);
    assertThat(newFile.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.TEMPORARY);

  }

  @Test
  public void processInitialUpload_failed() {

    var multiPartFile = mock(MultipartFile.class);

    var failedResult = FileUploadResult.generateFailedFileUploadResult(
        multiPartFile.getOriginalFilename(), multiPartFile, UploadErrorType.EXTENSION_NOT_ALLOWED);

    when(fileUploadService.processUpload(multiPartFile, wua)).thenReturn(failedResult);

    var fileUploadResult = appFileService.processInitialUpload(multiPartFile, application, AppFilePurpose.CASE_NOTES, wua);

    assertThat(fileUploadResult.isValid()).isFalse();

    verify(fileUploadService, times(1)).processUpload(multiPartFile, wua);
    verifyNoInteractions(appFileRepository);

  }

  @Test
  public void updateFiles_whenFilesNotOnForm_thenFilesAreDeleted() {

    var form = new AddCaseNoteForm();
    appFileService.updateFiles(
        form,
        application,
        AppFilePurpose.CASE_NOTES,
        FileUpdateMode.DELETE_UNLINKED_FILES,
        wua
    );

    verify(fileUploadService, times(1)).deleteUploadedFile(FILE_ID, wua);
    verify(appFileRepository, times(1)).deleteAll(Set.of(file));

  }

  @Test
  public void updateFiles_whenFileOnFormThenUpdatedDescriptionSaved_andLinkIsFull() {

    var form = new AddCaseNoteForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    appFileService.updateFiles(
        form,
        application,
        AppFilePurpose.CASE_NOTES,
        FileUpdateMode.DELETE_UNLINKED_FILES,
        wua
    );

    verify(appFileRepository, times(1)).saveAll(appFileSetCaptor.capture());

    var savedFiles = appFileSetCaptor.getValue();

    assertThat(savedFiles).isNotEmpty().allSatisfy(savedFile -> {
      assertThat(savedFile.getDescription()).isEqualTo("New Description");
      assertThat(savedFile.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.FULL);
    });

    verify(appFileRepository, times(1)).deleteAll(Collections.emptySet());

  }

  @Test
  public void updateFiles_whenNoExistingFiles() {

    when(appFileRepository.findAllByPwaApplicationAndPurpose(application, AppFilePurpose.CASE_NOTES))
        .thenReturn(List.of());

    var form = new AddCaseNoteForm();
    appFileService.updateFiles(form, application, AppFilePurpose.CASE_NOTES,
        FileUpdateMode.DELETE_UNLINKED_FILES, wua);

    verifyNoInteractions(fileUploadService);
    verify(appFileRepository, times(1)).saveAll(Set.of());
    verify(appFileRepository, times(1)).deleteAll(Set.of());

  }

  @Test
  public void deleteFileLinksAndUploadedFiles_uploadedFileRemoveSuccessful() {
    appFileService.deleteAppFileLinksAndUploadedFiles(List.of(file), wua);
    verify(appFileRepository).deleteAll(List.of(file));
  }

  @Test(expected = RuntimeException.class)
  public void deleteFileLinksAndUploadedFiles_uploadedFileRemoveFail() {

    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateFailedFileDeleteResult(invocation.getArgument(0))
    );

    appFileService.deleteAppFileLinksAndUploadedFiles(List.of(file), wua);

  }

  @Test
  public void processFileDeletion_verifyServiceInteractions() {

    appFileService.processFileDeletion(file, wua);

    verify(appFileRepository, times(1)).delete(file);
    verify(fileUploadService, times(1)).deleteUploadedFile(file.getFileId(), wua);

  }

  @Test
  public void getFilesLinkedToForm() {

    var form = new AddCaseNoteForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    when(appFileRepository.findAllAsFileViewByAppAndPurposeAndFileLinkStatus(
        application, AppFilePurpose.CASE_NOTES, ApplicationFileLinkStatus.ALL)).thenReturn(List.of(fileView));

    var result = appFileService.getFilesLinkedToForm(form, application, AppFilePurpose.CASE_NOTES);

    assertThat(result.get(0).getFileDescription()).isEqualTo("New Description");

  }

}