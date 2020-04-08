package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadBlockCrossingFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.BlockCrossingDocumentsForm;
import uk.co.ogauthority.pwa.repository.licence.PadCrossedBlockRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadBlockCrossingFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class BlockCrossingFileServiceTest {

  private final String FILE_ID = "1234567890qwertyuiop";
  @Mock
  private PadBlockCrossingFileRepository padBlockCrossingFileRepository;
  @Mock
  private PadCrossedBlockRepository padCrossedBlockRepository;
  @Mock
  private FileUploadService fileUploadService;
  @Mock
  private EntityManager entityManager;

  private BlockCrossingFileService blockCrossingFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PadBlockCrossingFile file;

  private WebUserAccount wua = new WebUserAccount(1);

  private UploadedFileView fileView = new UploadedFileView(
      FILE_ID,
      "NAME",
      100L,
      "DESC",
      Instant.now(),
      "");

  @Before
  public void setUp() throws Exception {

    blockCrossingFileService = new BlockCrossingFileService(
        padBlockCrossingFileRepository,
        padCrossedBlockRepository,
        fileUploadService,
        entityManager

    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    file = new PadBlockCrossingFile();
    file.setFileId(FILE_ID);

    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateSuccessfulFileDeleteResult(invocation.getArgument(0))
    );

    when(padBlockCrossingFileRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(file));
  }

  @Test
  public void getBlockCrossingFile_verifyServiceInteractions() {
    when(padBlockCrossingFileRepository.findByPwaApplicationDetailAndFileId(pwaApplicationDetail, FILE_ID))
        .thenReturn(Optional.of(file));
    blockCrossingFileService.getBlockCrossingFile(FILE_ID, pwaApplicationDetail);
    verify(padBlockCrossingFileRepository, times(1))
        .findByPwaApplicationDetailAndFileId(pwaApplicationDetail, FILE_ID);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getBlockCrossingFile_whenNotFound() {
    blockCrossingFileService.getBlockCrossingFile(FILE_ID, pwaApplicationDetail);
  }

  @Test
  public void deleteBlockCrossingFilesAndLinkedUploads_uploadedFileRemoveSuccessful() {
    blockCrossingFileService.deleteBlockCrossingFilesAndLinkedUploads(List.of(file), wua);
    verify(padBlockCrossingFileRepository).deleteAll(eq(List.of(file)));

  }

  @Test(expected = RuntimeException.class)
  public void deleteBlockCrossingFilesAndLinkedUploads_uploadedFileRemoveFail() {
    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateFailedFileDeleteResult(invocation.getArgument(0))
    );
    blockCrossingFileService.deleteBlockCrossingFilesAndLinkedUploads(List.of(file), wua);


  }

  @Test
  public void deleteBlockCrossingFileLink_verifyServiceInteraction() {

    blockCrossingFileService.deleteBlockCrossingFileLink(file);
    verify(padBlockCrossingFileRepository, times(1)).delete(file);
  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenFilesNotOnForm_thenFilesAreDeleted() {
    var form = new BlockCrossingDocumentsForm();
    blockCrossingFileService.updateOrDeleteLinkedFilesUsingForm(
        pwaApplicationDetail,
        form,
        wua
    );
    verify(fileUploadService, times(1)).deleteUploadedFile(FILE_ID, wua);
    verify(padBlockCrossingFileRepository, times(1)).deleteAll(Set.of(file));

  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenFileOnFormThenUpdatedDescriptionSaved_andLinkIsFull() {
    var form = new BlockCrossingDocumentsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    ArgumentCaptor<Set<PadBlockCrossingFile>> fileCapture = ArgumentCaptor.forClass(Set.class);

    blockCrossingFileService.updateOrDeleteLinkedFilesUsingForm(
        pwaApplicationDetail,
        form,
        wua
    );
    verify(padBlockCrossingFileRepository, times(1)).saveAll(fileCapture.capture());

    var savedFiles = fileCapture.getValue();
    assertThat(savedFiles).allSatisfy(savedFile -> {
      assertThat(savedFile.getDescription()).isEqualTo("New Description");
      assertThat(savedFile.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.FULL);
    });

    verify(padBlockCrossingFileRepository, times(1)).deleteAll(Collections.emptySet());

  }


  @Test
  public void getUpdatedBlockCrossingFileViewsWhenFileOnForm() {

    var fileViews = List.of(
        fileView
    );

    var form = new BlockCrossingDocumentsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    TypedQuery mockQuery = mock(TypedQuery.class);
    //noinspection unchecked
    when(entityManager.createQuery(any(), any())).thenReturn(mockQuery);
    when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
    when(mockQuery.getResultList()).thenReturn(fileViews);

    var result  = blockCrossingFileService.getUpdatedBlockCrossingFileViewsWhenFileOnForm(pwaApplicationDetail, form);

    assertThat(result.get(0).getFileDescription()).isEqualTo("New Description");

  }
}