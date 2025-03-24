package uk.co.ogauthority.pwa.features.application.tasks.optionstemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class OptionsTemplateServiceTest {

  @Mock
  private PadFileManagementService padFileManagementService;

  private OptionsTemplateService optionsTemplateService;

  private PwaApplicationDetail pwaApplicationDetail;
  private OptionsTemplateForm form;

  @BeforeEach
  void setUp() {

    optionsTemplateService = new OptionsTemplateService(padFileManagementService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    form = new OptionsTemplateForm();

  }

  @Test
  void isComplete() {

    optionsTemplateService.isComplete(pwaApplicationDetail);
    verify(padFileManagementService, times(1)).mapFilesToForm(any(), eq(pwaApplicationDetail), eq(FileDocumentType.OPTIONS_TEMPLATE));

  }

  @Test
  void validate_full_andDocumentProvidedWithDescription() {

    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    optionsTemplateService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_full_andDocumentProvidedWithoutDescription() {

    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    optionsTemplateService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  void validate_full_andZeroDocuments() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    optionsTemplateService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  void validate_partial_whenDocumentWithoutDescriptionProvided() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    optionsTemplateService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  void validate_partial_whenDocumentWithDescriptionProvided() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    optionsTemplateService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_partial_andZeroDocuments() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    optionsTemplateService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

}