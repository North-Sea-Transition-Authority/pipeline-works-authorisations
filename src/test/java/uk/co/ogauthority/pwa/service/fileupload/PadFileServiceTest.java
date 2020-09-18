package uk.co.ogauthority.pwa.service.fileupload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
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
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.file.PadFileRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.ProjectInformationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadFileServiceTest {

  private final String FILE_ID = "1234567890qwertyuiop";

  @Mock
  private PadFileRepository padFileRepository;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Captor
  private ArgumentCaptor<PadFile> padFileCaptor;

  @Captor
  private ArgumentCaptor<Set<PadFile>> padFileSetCaptor;

  private PadFileService padFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PadFile file;

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

    padFileService = new PadFileService(fileUploadService, padFileRepository, entityCopyingService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    file = new PadFile();
    file.setFileId(FILE_ID);
    file.setPurpose(ApplicationFilePurpose.LOCATION_DETAILS);

    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateSuccessfulFileDeleteResult(invocation.getArgument(0))
    );

    when(padFileRepository.findAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, ApplicationFilePurpose.LOCATION_DETAILS))
        .thenReturn(List.of(file));

    when(fileUploadService.createUploadFileWithDescriptionFormFromView(any())).thenCallRealMethod();

  }

  @Test
  public void mapFilesToForm() {

    var form = new LocationDetailsForm();

    when(padFileRepository.findAllAsFileViewByAppDetailAndPurposeAndFileLinkStatus(
        pwaApplicationDetail, ApplicationFilePurpose.LOCATION_DETAILS, ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    padFileService.mapFilesToForm(form, pwaApplicationDetail, ApplicationFilePurpose.LOCATION_DETAILS);

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

    var fileUploadResult = padFileService.processInitialUpload(multiPartFile, pwaApplicationDetail, ApplicationFilePurpose.LOCATION_DETAILS, wua);

    assertThat(fileUploadResult.isValid()).isTrue();

    verify(fileUploadService, times(1)).processUpload(multiPartFile, wua);
    verify(padFileRepository, times(1)).save(padFileCaptor.capture());

    var newFile = padFileCaptor.getValue();

    assertThat(newFile.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(newFile.getFileId()).isEqualTo(file.getFileId());
    assertThat(newFile.getDescription()).isNull();
    assertThat(newFile.getPurpose()).isEqualTo(ApplicationFilePurpose.LOCATION_DETAILS);
    assertThat(newFile.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.TEMPORARY);

  }

  @Test
  public void processInitialUpload_failed() {

    var multiPartFile = mock(MultipartFile.class);

    var failedResult = FileUploadResult.generateFailedFileUploadResult(
        multiPartFile.getOriginalFilename(), multiPartFile, UploadErrorType.EXTENSION_NOT_ALLOWED);

    when(fileUploadService.processUpload(multiPartFile, wua)).thenReturn(failedResult);

    var fileUploadResult = padFileService.processInitialUpload(multiPartFile, pwaApplicationDetail, ApplicationFilePurpose.LOCATION_DETAILS, wua);

    assertThat(fileUploadResult.isValid()).isFalse();

    verify(fileUploadService, times(1)).processUpload(multiPartFile, wua);
    verifyNoInteractions(padFileRepository);

  }

  @Test
  public void updateFiles_whenFilesNotOnForm_thenFilesAreDeleted() {

    var form = new LocationDetailsForm();
    padFileService.updateFiles(
        form,
        pwaApplicationDetail,
        ApplicationFilePurpose.LOCATION_DETAILS,
        FileUpdateMode.DELETE_UNLINKED_FILES,
        wua
    );

    verify(fileUploadService, times(1)).deleteUploadedFile(FILE_ID, wua);
    verify(padFileRepository, times(1)).deleteAll(Set.of(file));

  }

  @Test
  public void updateFiles_whenFileOnFormThenUpdatedDescriptionSaved_andLinkIsFull() {

    var form = new LocationDetailsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    padFileService.updateFiles(
        form,
        pwaApplicationDetail,
        ApplicationFilePurpose.LOCATION_DETAILS,
        FileUpdateMode.DELETE_UNLINKED_FILES,
        wua
    );

    verify(padFileRepository, times(1)).saveAll(padFileSetCaptor.capture());

    var savedFiles = padFileSetCaptor.getValue();

    assertThat(savedFiles).allSatisfy(savedFile -> {
      assertThat(savedFile.getDescription()).isEqualTo("New Description");
      assertThat(savedFile.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.FULL);
    });

    verify(padFileRepository, times(1)).deleteAll(Collections.emptySet());

  }

  @Test
  public void updateFiles_whenNoExistingFiles() {
    var form = ProjectInformationTestUtils.buildForm(LocalDate.now());
    padFileService.updateFiles(form, pwaApplicationDetail, ApplicationFilePurpose.PROJECT_INFORMATION,
        FileUpdateMode.DELETE_UNLINKED_FILES, wua);

    verifyNoInteractions(fileUploadService);
    verify(padFileRepository, times(1)).saveAll(eq(Set.of()));
    verify(padFileRepository, times(1)).deleteAll(eq(Set.of()));

  }

  @Test
  public void deleteFileLinksAndUploadedFiles_uploadedFileRemoveSuccessful() {
    padFileService.deleteAppFileLinksAndUploadedFiles(List.of(file), wua);
    verify(padFileRepository).deleteAll(eq(List.of(file)));
  }

  @Test(expected = RuntimeException.class)
  public void deleteFileLinksAndUploadedFiles_uploadedFileRemoveFail() {

    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateFailedFileDeleteResult(invocation.getArgument(0))
    );

    padFileService.deleteAppFileLinksAndUploadedFiles(List.of(file), wua);

  }

  @Test
  public void processFileDeletion_verifyServiceInteractions() {

    padFileService.processFileDeletion(file, wua);

    verify(padFileRepository, times(1)).delete(file);
    verify(fileUploadService, times(1)).deleteUploadedFile(file.getFileId(), wua);

  }

  @Test
  public void getFilesLinkedToForm() {

    var form = new LocationDetailsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    when(padFileRepository.findAllAsFileViewByAppDetailAndPurposeAndFileLinkStatus(
        pwaApplicationDetail, ApplicationFilePurpose.LOCATION_DETAILS, ApplicationFileLinkStatus.ALL)).thenReturn(List.of(fileView));

    var result = padFileService.getFilesLinkedToForm(form, pwaApplicationDetail, ApplicationFilePurpose.LOCATION_DETAILS);

    assertThat(result.get(0).getFileDescription()).isEqualTo("New Description");

  }

  @Test
  public void cleanupFiles_filesToKeep() {

    var file4 = new PadFile();
    file4.setPurpose(ApplicationFilePurpose.DEPOSIT_DRAWINGS);
    file4.setId(4);

    var file5 = new PadFile();
    file5.setPurpose(ApplicationFilePurpose.DEPOSIT_DRAWINGS);
    file5.setId(5);

    when(padFileRepository.findAllByAppDetailAndFilePurposeAndIdNotIn(pwaApplicationDetail, ApplicationFilePurpose.DEPOSIT_DRAWINGS, List.of(1, 2, 3)))
        .thenReturn(List.of(file4, file5));

    padFileService.cleanupFiles(pwaApplicationDetail, ApplicationFilePurpose.DEPOSIT_DRAWINGS, List.of(1, 2, 3));

    verify(padFileRepository, times(1)).deleteAll(eq(List.of(file4, file5)));

  }

  @Test
  public void cleanupFiles_noFilesToKeep() {

    var file1 = new PadFile();
    file1.setPurpose(ApplicationFilePurpose.DEPOSIT_DRAWINGS);
    file1.setId(1);

    var file2 = new PadFile();
    file2.setPurpose(ApplicationFilePurpose.DEPOSIT_DRAWINGS);
    file2.setId(2);

    var file3 = new PadFile();
    file3.setPurpose(ApplicationFilePurpose.DEPOSIT_DRAWINGS);
    file3.setId(3);

    when(padFileRepository.findAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, ApplicationFilePurpose.DEPOSIT_DRAWINGS))
        .thenReturn(List.of(file1, file2, file3));

    padFileService.cleanupFiles(pwaApplicationDetail, ApplicationFilePurpose.DEPOSIT_DRAWINGS, List.of());

    verify(padFileRepository, times(1)).deleteAll(eq(List.of(file1, file2, file3)));

  }

  @Test
  public void copyPadFilesToPwaApplicationDetail_serviceInteractions() {
    var newDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 20, 21);

    var copiedFiles = padFileService.copyPadFilesToPwaApplicationDetail(
        pwaApplicationDetail,
        newDetail,
        ApplicationFilePurpose.ADMIRALTY_CHART,
        ApplicationFileLinkStatus.FULL);

    verify(entityCopyingService, times(1)).duplicateEntitiesAndSetParent(
        any(),
        eq(newDetail),
        eq(PadFile.class)
    );


  }

}