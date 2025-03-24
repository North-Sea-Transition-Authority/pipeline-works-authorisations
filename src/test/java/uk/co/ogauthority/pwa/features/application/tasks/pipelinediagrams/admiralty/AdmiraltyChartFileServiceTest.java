package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class AdmiraltyChartFileServiceTest {

  @Mock
  private PadFileService padFileService;

  @Mock
  private PadFileManagementService padFileManagementService;

  private final SpringValidatorAdapter springValidatorAdapter = new SpringValidatorAdapter(
      Validation.buildDefaultValidatorFactory().getValidator());

  private AdmiraltyChartFileService admiraltyChartFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private final AdmiraltyChartDocumentForm form = new AdmiraltyChartDocumentForm();

  @BeforeEach
  void setUp() {
    admiraltyChartFileService = new AdmiraltyChartFileService(padFileService, padFileManagementService, springValidatorAdapter);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  void validate_full_whenNoDocumentRequired_andDocumentProvidedWithDescription() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_full_whenNoDocumentRequired_andDocumentProvidedWithoutDescription() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  void validate_full_whenDocumentRequired_andZeroDocuments() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  void validate_full_whenDocumentRequired_andDocumentWithDescriptionProvided() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_partial_whenDocumentWithoutDescriptionProvided() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  void validate_partial_whenDocumentWithDescriptionProvided() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void getAdmiraltyChartFile_present() {

    var admiraltyChartFile = new UploadedFileView(
        null,
        null,
        1L,
        "admiralty desc",
        null,
        null
    );

    when(padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.ADMIRALTY_CHART))
        .thenReturn(List.of(admiraltyChartFile));

    assertThat(admiraltyChartFileService.getAdmiraltyChartFile(pwaApplicationDetail)).contains(admiraltyChartFile);

  }

  @Test
  void getAdmiraltyChartFile_no_empty() {

    when(padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.ADMIRALTY_CHART))
        .thenReturn(List.of());

    assertThat(admiraltyChartFileService.getAdmiraltyChartFile(pwaApplicationDetail)).isEmpty();

  }

}