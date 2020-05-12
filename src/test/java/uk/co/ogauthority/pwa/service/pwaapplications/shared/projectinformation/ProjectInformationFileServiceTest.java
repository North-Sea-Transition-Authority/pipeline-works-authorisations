package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformationFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.file.PadProjectInformationFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.service.fileupload.PwaApplicationFileService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ProjectInformationFileServiceTest {

  private static final String FILE_ID = "file_123456789abcdefg";
  private static final String FILE_NAME = "filename.jpg";
  private static final String FILE_DESC = "DESC";
  private static final Instant FILE_CREATION_INSTANT = Instant.now();

  @Mock
  private PadProjectInformationFileRepository padProjectInformationFileRepository;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private EntityManager entityManager;

  @Mock
  private PwaApplicationFileService pwaApplicationFileService;

  private ProjectInformationFileService projectInformationFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private WebUserAccount user = new WebUserAccount(1);

  @Before
  public void setUp() {
    projectInformationFileService = new ProjectInformationFileService(
        padProjectInformationFileRepository,
        fileUploadService,
        entityManager,
        pwaApplicationFileService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    // just return whatever is given
    when(padProjectInformationFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateSuccessfulFileDeleteResult(invocation.getArgument(0))
    );
  }

  @Test
  public void createAndSaveProjectInformationFile_returnsNewProjectInformationFile() {


    assertThat(projectInformationFileService.createAndSaveProjectInformationFile(
        pwaApplicationDetail,
        FILE_ID
    )).isNotNull();

  }


  @Test
  public void createAndSaveProjectInformationFile_peristsCreatedObject() {

    projectInformationFileService.createAndSaveProjectInformationFile(
        pwaApplicationDetail,
        FILE_ID
    );

    ArgumentCaptor<PadProjectInformationFile> capturedArg = ArgumentCaptor.forClass(PadProjectInformationFile.class);

    verify(padProjectInformationFileRepository, times(1)).save(capturedArg.capture());
  }

  @Test
  public void createAndSaveProjectInformationFile_createdObjectHasCorrectlyMappedAttributes() {

    var createdObject = projectInformationFileService.createAndSaveProjectInformationFile(
        pwaApplicationDetail,
        FILE_ID
    );

    assertThat(createdObject.getFileId()).isEqualTo(FILE_ID);
    assertThat(createdObject.getDescription()).isNull();
    assertThat(createdObject.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.TEMPORARY);
    assertThat(createdObject.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);

  }


  @Test(expected = PwaEntityNotFoundException.class)
  public void getProjectInformationFile_whenFileNotFound() {
    projectInformationFileService.getProjectInformationFile(FILE_ID, pwaApplicationDetail);
  }

  @Test
  public void getProjectInformationFile_whenFileFound() {
    when(padProjectInformationFileRepository.findByPwaApplicationDetailAndFileId(pwaApplicationDetail,
        FILE_ID)).thenReturn(
        Optional.of(new PadProjectInformationFile())
    );

    assertThat(projectInformationFileService.getProjectInformationFile(FILE_ID, pwaApplicationDetail)).isNotNull();
  }

  @Test
  public void deleteProjectInformationFilesAndLinkedUploads_allFilesProcessed() {
    var file1 = new PadProjectInformationFile(
        pwaApplicationDetail,
        FILE_ID + "1",
        "one",
        ApplicationFileLinkStatus.TEMPORARY
    );

    var file2 = new PadProjectInformationFile(
        pwaApplicationDetail,
        FILE_ID + "2",
        "two",
        ApplicationFileLinkStatus.TEMPORARY
    );

    var fileLinkList = List.of(file1, file2);

    projectInformationFileService.deleteProjectInformationFilesAndLinkedUploads(fileLinkList, user);

    ArgumentCaptor<String> fileIdArgCapture = ArgumentCaptor.forClass(String.class);
    verify(fileUploadService, times(2)).deleteUploadedFile(fileIdArgCapture.capture(), eq(user));
    verify(padProjectInformationFileRepository, times(1)).deleteAll(List.of(file1, file2));

    assertThat(fileIdArgCapture.getAllValues()).containsExactlyInAnyOrder(file1.getFileId(), file2.getFileId());
  }

  @Test(expected = RuntimeException.class)
  public void deleteProjectInformationFilesAndLinkedUploads_whenDeleteFails() {
    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateFailedFileDeleteResult(invocation.getArgument(0))
    );

    var file1 = new PadProjectInformationFile(
        pwaApplicationDetail,
        FILE_ID + "1",
        "one",
        ApplicationFileLinkStatus.TEMPORARY
    );

    var fileLinkList = List.of(file1);

    projectInformationFileService.deleteProjectInformationFilesAndLinkedUploads(fileLinkList, user);

  }

  @Test
  public void deleteProjectInformationFileLink_verifyRepoCalled() {
    var toDeleteFile = new PadProjectInformationFile();
    projectInformationFileService.deleteProjectInformationFileLink(toDeleteFile);
    verify(padProjectInformationFileRepository, times(1)).delete(toDeleteFile);
  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenNoExistingFiles() {
    var form = ProjectInformationTestUtils.buildForm(LocalDate.now());
    projectInformationFileService.updateOrDeleteLinkedFilesUsingForm(pwaApplicationDetail, form, user);

    verifyNoInteractions(fileUploadService);
    verify(padProjectInformationFileRepository, times(1)).saveAll(eq(Set.of()));
    verify(padProjectInformationFileRepository, times(1)).deleteAll(eq(Set.of()));

  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenSingleExistingFile_andFormUpdatesDescription() {
    var form = ProjectInformationTestUtils.buildForm(LocalDate.now());

    var formFile = new UploadFileWithDescriptionForm(FILE_ID, "New Description", FILE_CREATION_INSTANT);
    var linkedFile = new PadProjectInformationFile(pwaApplicationDetail, FILE_ID, FILE_DESC,
        ApplicationFileLinkStatus.TEMPORARY);

    form.setUploadedFileWithDescriptionForms(List.of(formFile));
    when(padProjectInformationFileRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        List.of(linkedFile));

    projectInformationFileService.updateOrDeleteLinkedFilesUsingForm(pwaApplicationDetail, form, user);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Set<PadProjectInformationFile>> savedFilesCapture = ArgumentCaptor.forClass(Set.class);

    verifyNoInteractions(fileUploadService);
    verify(padProjectInformationFileRepository, times(1)).saveAll(savedFilesCapture.capture());
    verify(padProjectInformationFileRepository, times(1)).deleteAll(eq(Set.of()));

    assertThat(savedFilesCapture.getValue().size()).isEqualTo(1);
    assertThat(savedFilesCapture.getValue()).allSatisfy(element -> {
      assertThat(element.getDescription()).isEqualTo(formFile.getUploadedFileDescription());
      assertThat(element.getFileId()).isEqualTo(formFile.getUploadedFileId());
    });

  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenSingleExistingFile_andFormContainsNoFiles() {
    var form = ProjectInformationTestUtils.buildForm(LocalDate.now());
    var linkedFile = new PadProjectInformationFile(pwaApplicationDetail, FILE_ID, FILE_DESC,
        ApplicationFileLinkStatus.TEMPORARY);

    when(padProjectInformationFileRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        List.of(linkedFile));

    projectInformationFileService.updateOrDeleteLinkedFilesUsingForm(pwaApplicationDetail, form, user);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Set<PadProjectInformationFile>> savedFilesCapture = ArgumentCaptor.forClass(Set.class);

    verify(padProjectInformationFileRepository, times(1)).saveAll(eq(Set.of()));
    verify(padProjectInformationFileRepository, times(1)).deleteAll(savedFilesCapture.capture());
    verify(fileUploadService, times(1)).deleteUploadedFile(linkedFile.getFileId(), user);

    assertThat(savedFilesCapture.getValue().size()).isEqualTo(1);
    assertThat(savedFilesCapture.getValue()).allSatisfy(element -> {
      assertThat(element).isEqualTo(linkedFile);
    });

  }

  @Test
  public void getUpdatedProjectInformationFileViewsWhenFileOnForm_whenFormFileHasUpdatedDescriptionItIsMapped() {
    List<UploadedFileView> fakeExistingFileViews = List.of(
        new UploadedFileView(FILE_ID, FILE_NAME, 100L, FILE_DESC, FILE_CREATION_INSTANT, "#")
    );

    TypedQuery mockQuery = mock(TypedQuery.class);
    //noinspection unchecked
    when(entityManager.createQuery(any(), any())).thenReturn(mockQuery);
    when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
    when(mockQuery.getResultList()).thenReturn(fakeExistingFileViews);

    var form = ProjectInformationTestUtils.buildForm(LocalDate.now());
    var formFile = new UploadFileWithDescriptionForm(FILE_ID, "New Description", FILE_CREATION_INSTANT);

    form.setUploadedFileWithDescriptionForms(List.of(formFile));

    var updatedFiles = projectInformationFileService.getUpdatedProjectInformationFileViewsWhenFileOnForm(
        pwaApplicationDetail, form);

    assertThat(updatedFiles.get(0).getFileDescription()).isEqualTo(formFile.getUploadedFileDescription());
  }

  @Test
  public void getUpdatedProjectInformationFileViewsWhenFileOnForm_whenFormHasNoFiles() {

    TypedQuery mockQuery = mock(TypedQuery.class);
    //noinspection unchecked
    when(entityManager.createQuery(any(), any())).thenReturn(mockQuery);
    when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
    when(mockQuery.getResultList()).thenReturn(List.of());

    var form = ProjectInformationTestUtils.buildForm(LocalDate.now());

    var updatedFiles = projectInformationFileService.getUpdatedProjectInformationFileViewsWhenFileOnForm(
        pwaApplicationDetail, form);

    assertThat(updatedFiles).isEmpty();
  }

  @Test
  public void getUploadedFileListAsFormList_createsFormObjectsAsExpected() {

    var uploadedFileView = new UploadedFileView(FILE_ID, FILE_NAME, 100L, FILE_DESC, FILE_CREATION_INSTANT, "#");
    List<UploadedFileView> fakeExistingFileViews = List.of(uploadedFileView);
    when(fileUploadService.createUploadFileWithDescriptionFormFromView(any())).thenCallRealMethod();

    TypedQuery mockQuery = mock(TypedQuery.class);
    //noinspection unchecked
    when(entityManager.createQuery(any(), any())).thenReturn(mockQuery);
    when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
    when(mockQuery.getResultList()).thenReturn(fakeExistingFileViews);

    var filesAsFormList = projectInformationFileService.getUploadedFileListAsFormList(
        pwaApplicationDetail,
        ApplicationFileLinkStatus.ALL
    );

    assertThat(filesAsFormList).allSatisfy(fileForm -> {
      assertThat(fileForm.getUploadedFileDescription()).isEqualTo(uploadedFileView.getFileDescription());
      assertThat(fileForm.getUploadedFileId()).isEqualTo(uploadedFileView.getFileId());
      assertThat(fileForm.getUploadedFileInstant()).isEqualTo(uploadedFileView.getFileUploadedTime());
    });
  }

  @Test
  public void getProjectInformationFileViews_setsDownloadLinkAsExpected() {
    List<UploadedFileView> fakeExistingFileViews = List.of(
        new UploadedFileView(FILE_ID, FILE_NAME, 100L, FILE_DESC, FILE_CREATION_INSTANT, "#")
    );

    TypedQuery mockQuery = mock(TypedQuery.class);
    //noinspection unchecked
    when(entityManager.createQuery(any(), any())).thenReturn(mockQuery);
    when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
    when(mockQuery.getResultList()).thenReturn(fakeExistingFileViews);


    var fileViewList = projectInformationFileService.getProjectInformationFileViews(pwaApplicationDetail, ApplicationFileLinkStatus.ALL);

    assertThat(fileViewList).allSatisfy(fileView -> {
      assertThat(fileView.getFileUrl()).isEqualTo(ReverseRouter.route(on(ProjectInformationController.class)
          .handleDownload(
              pwaApplicationDetail.getPwaApplicationType(),
              pwaApplicationDetail.getMasterPwaApplicationId(),
              fileView.getFileId(),
              null
              )
      ));
    });
  }
}