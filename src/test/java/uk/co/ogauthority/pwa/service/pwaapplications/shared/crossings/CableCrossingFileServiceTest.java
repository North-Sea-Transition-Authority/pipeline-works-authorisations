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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossingFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadCableCrossingFileRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadCableCrossingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CableCrossingFileServiceTest {

  private final String FILE_ID = "1234567890qwertyuiop";

  @Mock
  private PadCableCrossingFileRepository padCableCrossingFileRepository;

  @Mock
  private PadCableCrossingRepository padCableCrossingRepository;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private EntityManager entityManager;

  private SpringValidatorAdapter springValidatorAdapter = new SpringValidatorAdapter(
      Validation.buildDefaultValidatorFactory().getValidator());

  private CableCrossingFileService cableCrossingFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PadCableCrossingFile file;

  private WebUserAccount wua = new WebUserAccount(1);

  private UploadedFileView fileView = new UploadedFileView(
      FILE_ID,
      "NAME",
      100L,
      "DESC",
      Instant.now(),
      "");

  private CrossingDocumentsForm form = new CrossingDocumentsForm();

  @Before
  public void setUp() {

    cableCrossingFileService = new CableCrossingFileService(
        padCableCrossingFileRepository,
        padCableCrossingRepository,
        fileUploadService,
        entityManager,
        springValidatorAdapter);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    file = new PadCableCrossingFile();
    file.setFileId(FILE_ID);

    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateSuccessfulFileDeleteResult(invocation.getArgument(0))
    );

    when(padCableCrossingFileRepository.findAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(file));
  }

  @Test
  public void getCableCrossingFile_verifyServiceInteractions() {
    when(padCableCrossingFileRepository.findByPwaApplicationDetailAndFileId(pwaApplicationDetail, FILE_ID))
        .thenReturn(Optional.of(file));
    cableCrossingFileService.getCableCrossingFile(FILE_ID, pwaApplicationDetail);
    verify(padCableCrossingFileRepository, times(1))
        .findByPwaApplicationDetailAndFileId(pwaApplicationDetail, FILE_ID);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getCableCrossingFile_whenNotFound() {
    cableCrossingFileService.getCableCrossingFile(FILE_ID, pwaApplicationDetail);
  }

  @Test
  public void deleteCableCrossingFilesAndLinkedUploads_uploadedFileRemoveSuccessful() {
    cableCrossingFileService.deleteCableCrossingFilesAndLinkedUploads(List.of(file), wua);
    verify(padCableCrossingFileRepository).deleteAll(eq(List.of(file)));
  }

  @Test(expected = RuntimeException.class)
  public void deleteCableCrossingFilesAndLinkedUploads_uploadedFileRemoveFail() {
    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateFailedFileDeleteResult(invocation.getArgument(0))
    );
    cableCrossingFileService.deleteCableCrossingFilesAndLinkedUploads(List.of(file), wua);
  }

  @Test
  public void deleteCableCrossingFileLink_verifyServiceInteraction() {
    cableCrossingFileService.deleteCableCrossingFileLink(file);
    verify(padCableCrossingFileRepository, times(1)).delete(file);
  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenFilesNotOnForm_thenFilesAreDeleted() {
    var form = new CrossingDocumentsForm();
    cableCrossingFileService.updateOrDeleteLinkedFilesUsingForm(
        pwaApplicationDetail,
        form,
        wua
    );
    verify(fileUploadService, times(1)).deleteUploadedFile(FILE_ID, wua);
    verify(padCableCrossingFileRepository, times(1)).deleteAll(Set.of(file));
  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenFileOnFormThenUpdatedDescriptionSaved_andLinkIsFull() {
    var form = new CrossingDocumentsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    ArgumentCaptor<Set<PadCableCrossingFile>> fileCapture = ArgumentCaptor.forClass(Set.class);

    cableCrossingFileService.updateOrDeleteLinkedFilesUsingForm(
        pwaApplicationDetail,
        form,
        wua
    );
    verify(padCableCrossingFileRepository, times(1)).saveAll(fileCapture.capture());

    var savedFiles = fileCapture.getValue();
    assertThat(savedFiles).allSatisfy(savedFile -> {
      assertThat(savedFile.getDescription()).isEqualTo("New Description");
      assertThat(savedFile.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.FULL);
    });

    verify(padCableCrossingFileRepository, times(1)).deleteAll(Collections.emptySet());
  }

  @Test
  public void getUpdatedCableCrossingFileViewsWhenFileOnForm() {
    var fileViews = List.of(
        fileView
    );

    var form = new CrossingDocumentsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    TypedQuery mockQuery = mock(TypedQuery.class);
    //noinspection unchecked
    when(entityManager.createQuery(any(), any())).thenReturn(mockQuery);
    when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
    when(mockQuery.getResultList()).thenReturn(fileViews);

    var result = cableCrossingFileService.getUpdatedCableCrossingFileViewsWhenFileOnForm(pwaApplicationDetail,
        form);
    assertThat(result.get(0).getFileDescription()).isEqualTo("New Description");
  }

  @Test
  public void validate_full_whenNoDocumentRequired_andDocumentProvidedWithDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "2", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validate_full_whenNoDocumentRequired_andDocumentProvidedWithoutDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_full_whenDocumentRequired_andZeroDocuments() {
    when(padCableCrossingRepository.countAllByPwaApplicationDetail(eq(pwaApplicationDetail))).thenReturn(1);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_full_whenDocumentRequired_andDocumentWithDescriptionProvided() {
    when(padCableCrossingRepository.countAllByPwaApplicationDetail(eq(pwaApplicationDetail))).thenReturn(1);
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validate_partial_whenDocumentWithoutDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_partial_whenDocumentWithDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

}