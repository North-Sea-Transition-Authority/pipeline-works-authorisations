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
import javax.validation.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadMedianLineCrossingFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.MedianLineCrossingDocumentsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadMedianLineCrossingFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class MedianLineCrossingFileServiceTest {

  private final String FILE_ID = "1234567890qwertyuiop";

  @Mock
  private PadMedianLineCrossingFileRepository padMedianLineCrossingFileRepository;
  @Mock
  private FileUploadService fileUploadService;
  @Mock
  private EntityManager entityManager;

  private SpringValidatorAdapter springValidatorAdapter = new SpringValidatorAdapter(
      Validation.buildDefaultValidatorFactory().getValidator());

  private MedianLineCrossingFileService medianLineCrossingFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PadMedianLineCrossingFile file;

  private WebUserAccount wua = new WebUserAccount(1);

  private UploadedFileView fileView = new UploadedFileView(
      FILE_ID,
      "NAME",
      100L,
      "DESC",
      Instant.now(),
      "");

  private MedianLineCrossingDocumentsForm form = new MedianLineCrossingDocumentsForm();

  @Before
  public void setUp() {

    medianLineCrossingFileService = new MedianLineCrossingFileService(
        padMedianLineCrossingFileRepository,
        fileUploadService,
        entityManager,
        springValidatorAdapter);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    file = new PadMedianLineCrossingFile();
    file.setFileId(FILE_ID);

    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateSuccessfulFileDeleteResult(invocation.getArgument(0))
    );

    when(padMedianLineCrossingFileRepository.findAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(file));
  }

  @Test
  public void getMedianLineCrossingFile_verifyServiceInteractions() {
    when(padMedianLineCrossingFileRepository.findByPwaApplicationDetailAndFileId(pwaApplicationDetail, FILE_ID))
        .thenReturn(Optional.of(file));
    medianLineCrossingFileService.getMedianLineCrossingFile(FILE_ID, pwaApplicationDetail);
    verify(padMedianLineCrossingFileRepository, times(1))
        .findByPwaApplicationDetailAndFileId(pwaApplicationDetail, FILE_ID);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getMedianLineCrossingFile_whenNotFound() {
    medianLineCrossingFileService.getMedianLineCrossingFile(FILE_ID, pwaApplicationDetail);
  }

  @Test
  public void deleteMedianLineCrossingFilesAndLinkedUploads_uploadedFileRemoveSuccessful() {
    medianLineCrossingFileService.deleteMedianLineCrossingFilesAndLinkedUploads(List.of(file), wua);
    verify(padMedianLineCrossingFileRepository).deleteAll(eq(List.of(file)));
  }

  @Test(expected = RuntimeException.class)
  public void deleteMedianLineCrossingFilesAndLinkedUploads_uploadedFileRemoveFail() {
    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateFailedFileDeleteResult(invocation.getArgument(0))
    );
    medianLineCrossingFileService.deleteMedianLineCrossingFilesAndLinkedUploads(List.of(file), wua);
  }

  @Test
  public void deleteMedianLineCrossingFileLink_verifyServiceInteraction() {
    medianLineCrossingFileService.deleteMedianLineCrossingFileLink(file);
    verify(padMedianLineCrossingFileRepository, times(1)).delete(file);
  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenFilesNotOnForm_thenFilesAreDeleted() {
    var form = new MedianLineCrossingDocumentsForm();
    medianLineCrossingFileService.updateOrDeleteLinkedFilesUsingForm(
        pwaApplicationDetail,
        form,
        wua
    );
    verify(fileUploadService, times(1)).deleteUploadedFile(FILE_ID, wua);
    verify(padMedianLineCrossingFileRepository, times(1)).deleteAll(Set.of(file));
  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenFileOnFormThenUpdatedDescriptionSaved_andLinkIsFull() {
    var form = new MedianLineCrossingDocumentsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    ArgumentCaptor<Set<PadMedianLineCrossingFile>> fileCapture = ArgumentCaptor.forClass(Set.class);

    medianLineCrossingFileService.updateOrDeleteLinkedFilesUsingForm(
        pwaApplicationDetail,
        form,
        wua
    );
    verify(padMedianLineCrossingFileRepository, times(1)).saveAll(fileCapture.capture());

    var savedFiles = fileCapture.getValue();
    assertThat(savedFiles).allSatisfy(savedFile -> {
      assertThat(savedFile.getDescription()).isEqualTo("New Description");
      assertThat(savedFile.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.FULL);
    });

    verify(padMedianLineCrossingFileRepository, times(1)).deleteAll(Collections.emptySet());
  }

  @Test
  public void getUpdatedMedianLineCrossingFileViewsWhenFileOnForm() {
    var fileViews = List.of(
        fileView
    );

    var form = new MedianLineCrossingDocumentsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    TypedQuery mockQuery = mock(TypedQuery.class);
    //noinspection unchecked
    when(entityManager.createQuery(any(), any())).thenReturn(mockQuery);
    when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
    when(mockQuery.getResultList()).thenReturn(fileViews);

    var result = medianLineCrossingFileService.getUpdatedMedianLineCrossingFileViewsWhenFileOnForm(pwaApplicationDetail,
        form);
    assertThat(result.get(0).getFileDescription()).isEqualTo("New Description");
  }

  @Test
  public void validate_full_whenNoDocumentRequired_andDocumentProvidedWithDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "2", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    medianLineCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validate_full_whenNoDocumentRequired_andDocumentProvidedWithoutDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    medianLineCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_full_whenDocumentRequired_andZeroDocuments() {
    when(padMedianLineCrossingFileRepository.countAllByPwaApplicationDetailAndFileLinkStatus(eq(pwaApplicationDetail),
        any())).thenReturn(1);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    medianLineCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_full_whenDocumentRequired_andDocumentWithDescriptionProvided() {
    when(padMedianLineCrossingFileRepository.countAllByPwaApplicationDetailAndFileLinkStatus(eq(pwaApplicationDetail),
        any())).thenReturn(1);
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    medianLineCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validate_partial_whenDocumentWithoutDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    medianLineCrossingFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_partial_whenDocumentWithDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    medianLineCrossingFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void getFullFileCount() {
    when(padMedianLineCrossingFileRepository.countAllByPwaApplicationDetailAndFileLinkStatus(pwaApplicationDetail,
        ApplicationFileLinkStatus.FULL)).thenReturn(1);
    var result = medianLineCrossingFileService.getFullFileCount(pwaApplicationDetail);
    assertThat(result).isEqualTo(1);
  }

}