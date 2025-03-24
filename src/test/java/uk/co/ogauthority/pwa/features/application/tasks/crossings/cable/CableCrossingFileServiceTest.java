package uk.co.ogauthority.pwa.features.application.tasks.crossings.cable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.formhelpers.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class CableCrossingFileServiceTest {

  @Mock
  private PadCableCrossingRepository padCableCrossingRepository;

  @Mock
  private PadFileManagementService padFileManagementService;

  private CableCrossingFileService cableCrossingFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private CrossingDocumentsForm form = new CrossingDocumentsForm();

  @BeforeEach
  void setUp() {

    cableCrossingFileService = new CableCrossingFileService(padCableCrossingRepository, padFileManagementService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  void validate_full_whenNoDocumentRequired_andDocumentProvidedWithDescription() {

    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_full_whenNoDocumentRequired_andDocumentProvidedWithoutDescription() {

    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  void validate_full_whenDocumentRequired_andZeroDocuments() {
    when(padCableCrossingRepository.countAllByPwaApplicationDetail(eq(pwaApplicationDetail))).thenReturn(1);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  void validate_full_whenDocumentRequired_andDocumentWithDescriptionProvided() {
    when(padCableCrossingRepository.countAllByPwaApplicationDetail(eq(pwaApplicationDetail))).thenReturn(1);
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_partial_whenDocumentWithoutDescriptionProvided() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  void validate_partial_whenDocumentWithDescriptionProvided() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_full_existingDocumentDeleted_newDocumentAdded_noErrors() {

    var existingDocumentDeleted = FileManagementValidatorTestUtils.createUploadedFileForm();
    var newDocAdded = FileManagementValidatorTestUtils.createUploadedFileForm();
    form.setUploadedFiles(List.of(existingDocumentDeleted, newDocAdded));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_full_existingDocumentDeleted_newDocumentAdded_noDescription_error() {

    var existingDocumentDeleted = FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription();
    var newDocAdded = FileManagementValidatorTestUtils.createUploadedFileForm();
    form.setUploadedFiles(List.of(existingDocumentDeleted, newDocAdded));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  void isComplete_serviceInteraction() {
    var result = cableCrossingFileService.isComplete(pwaApplicationDetail);
    verify(padFileManagementService, times(1)).mapFilesToForm(any(), eq(pwaApplicationDetail), eq(FileDocumentType.CABLE_CROSSINGS));
    assertThat(result).isTrue();
  }

}