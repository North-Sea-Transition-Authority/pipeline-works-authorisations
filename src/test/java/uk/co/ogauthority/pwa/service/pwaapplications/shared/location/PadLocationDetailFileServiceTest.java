package uk.co.ogauthority.pwa.service.pwaapplications.shared.location;

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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.location.PadLocationDetailFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailDocumentsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadLocationDetailFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadLocationDetailFileServiceTest {

  private final String FILE_ID = "1234567890qwertyuiop";
  @Mock
  private PadLocationDetailFileRepository padLocationDetailFileRepository;
  @Mock
  private FileUploadService fileUploadService;
  @Mock
  private EntityManager entityManager;


  private SpringValidatorAdapter springValidatorAdapter = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

  private PadLocationDetailFileService padLocationDetailFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PadLocationDetailFile file;

  private WebUserAccount wua = new WebUserAccount(1);

  private UploadedFileView fileView = new UploadedFileView(
      FILE_ID,
      "NAME",
      100L,
      "DESC",
      Instant.now(),
      "");

  private LocationDetailDocumentsForm form = new LocationDetailDocumentsForm();

  @Before
  public void setUp() {

    padLocationDetailFileService = new PadLocationDetailFileService(
        padLocationDetailFileRepository,
        fileUploadService,
        entityManager,
        springValidatorAdapter);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    file = new PadLocationDetailFile();
    file.setFileId(FILE_ID);

    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateSuccessfulFileDeleteResult(invocation.getArgument(0))
    );

    when(padLocationDetailFileRepository.findAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(file));
  }

  @Test
  public void getLocationDetailFile_verifyServiceInteractions() {
    when(padLocationDetailFileRepository.findByPwaApplicationDetailAndFileId(pwaApplicationDetail, FILE_ID))
        .thenReturn(Optional.of(file));
    padLocationDetailFileService.getLocationDetailFile(FILE_ID, pwaApplicationDetail);
    verify(padLocationDetailFileRepository, times(1))
        .findByPwaApplicationDetailAndFileId(pwaApplicationDetail, FILE_ID);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getLocationDetailFile_whenNotFound() {
    padLocationDetailFileService.getLocationDetailFile(FILE_ID, pwaApplicationDetail);
  }

  @Test
  public void deleteLocationDetailFilesAndLinkedUploads_uploadedFileRemoveSuccessful() {
    padLocationDetailFileService.deleteLocationDetailFilesAndLinkedUploads(List.of(file), wua);
    verify(padLocationDetailFileRepository).deleteAll(eq(List.of(file)));
  }

  @Test(expected = RuntimeException.class)
  public void deleteLocationDetailFilesAndLinkedUploads_uploadedFileRemoveFail() {
    when(fileUploadService.deleteUploadedFile(any(), any())).thenAnswer(invocation ->
        FileDeleteResult.generateFailedFileDeleteResult(invocation.getArgument(0))
    );
    padLocationDetailFileService.deleteLocationDetailFilesAndLinkedUploads(List.of(file), wua);
  }

  @Test
  public void deleteLocationDetailFileLink_verifyServiceInteraction() {
    padLocationDetailFileService.deleteLocationDetailFileLink(file);
    verify(padLocationDetailFileRepository, times(1)).delete(file);
  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenFilesNotOnForm_thenFilesAreDeleted() {
    var form = new LocationDetailDocumentsForm();
    padLocationDetailFileService.updateOrDeleteLinkedFilesUsingForm(
        pwaApplicationDetail,
        form,
        wua
    );
    verify(fileUploadService, times(1)).deleteUploadedFile(FILE_ID, wua);
    verify(padLocationDetailFileRepository, times(1)).deleteAll(Set.of(file));
  }

  @Test
  public void updateOrDeleteLinkedFilesUsingForm_whenFileOnFormThenUpdatedDescriptionSaved_andLinkIsFull() {
    var form = new LocationDetailDocumentsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    ArgumentCaptor<Set<PadLocationDetailFile>> fileCapture = ArgumentCaptor.forClass(Set.class);

    padLocationDetailFileService.updateOrDeleteLinkedFilesUsingForm(
        pwaApplicationDetail,
        form,
        wua
    );
    verify(padLocationDetailFileRepository, times(1)).saveAll(fileCapture.capture());

    var savedFiles = fileCapture.getValue();
    assertThat(savedFiles).allSatisfy(savedFile -> {
      assertThat(savedFile.getDescription()).isEqualTo("New Description");
      assertThat(savedFile.getFileLinkStatus()).isEqualTo(ApplicationFileLinkStatus.FULL);
    });

    verify(padLocationDetailFileRepository, times(1)).deleteAll(Collections.emptySet());
  }


  @Test
  public void getUpdatedLocationDetailFileViewsWhenFileOnForm() {

    var fileViews = List.of(
        fileView
    );

    var form = new LocationDetailDocumentsForm();
    var fileForm = new UploadFileWithDescriptionForm(FILE_ID, "New Description", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(fileForm));

    TypedQuery mockQuery = mock(TypedQuery.class);
    //noinspection unchecked
    when(entityManager.createQuery(any(), any())).thenReturn(mockQuery);
    when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
    when(mockQuery.getResultList()).thenReturn(fileViews);

    var result = padLocationDetailFileService.getUpdatedLocationDetailFileViewsWhenFileOnForm(pwaApplicationDetail, form);

    assertThat(result.get(0).getFileDescription()).isEqualTo("New Description");
  }

  @Test
  public void validate_full_whenNoDocumentRequired_andDocumentProvidedWithDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "2", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padLocationDetailFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validate_full_whenNoDocumentRequired_andDocumentProvidedWithoutDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padLocationDetailFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_full_whenDocumentRequired_andDocumentWithDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padLocationDetailFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validate_partial_whenDocumentWithoutDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padLocationDetailFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_partial_whenDocumentWithDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padLocationDetailFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

}