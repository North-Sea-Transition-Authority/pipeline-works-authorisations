package uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs;

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
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class SupplementaryDocumentsServiceTest {

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private PadFileManagementService padFileManagementService;

  private SupplementaryDocumentsService supplementaryDocumentsService;

  private PwaApplicationDetail pwaApplicationDetail;
  private SupplementaryDocumentsForm form;

  @BeforeEach
  void setUp() {

    supplementaryDocumentsService = new SupplementaryDocumentsService(detailService, padFileManagementService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    form = new SupplementaryDocumentsForm();

  }

  @Test
  void isComplete() {

    supplementaryDocumentsService.isComplete(pwaApplicationDetail);
    verify(padFileManagementService, times(1)).mapFilesToForm(any(), eq(pwaApplicationDetail), eq(FileDocumentType.SUPPLEMENTARY_DOCUMENTS));

  }

  @Test
  void validate_full_noDataEntered() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  void validate_full_noDocsToUpload() {

    form.setHasFilesToUpload(false);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_full_docsToUpload_noDocsProvided() {

    form.setHasFilesToUpload(true);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  void validate_full_docsToUpload_docsProvided() {

    form.setHasFilesToUpload(true);
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_partial_noDataEntered() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_partial_noDocsToUpload() {

    form.setHasFilesToUpload(false);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_partial_docsToUpload_noDocsProvided() {

    form.setHasFilesToUpload(true);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_partial_docsToUpload_docsProvided() {

    form.setHasFilesToUpload(true);
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void updateDocumentFlag() {
    form.setHasFilesToUpload(false);
    supplementaryDocumentsService.updateDocumentFlag(pwaApplicationDetail, form);
    verify(detailService, times(1)).setSupplementaryDocumentsFlag(pwaApplicationDetail, form.getHasFilesToUpload());
  }

  @Test
  void mapSavedDataToForm() {

    pwaApplicationDetail.setSupplementaryDocumentsFlag(false);

    supplementaryDocumentsService.mapSavedDataToForm(pwaApplicationDetail, form);

    form.setHasFilesToUpload(pwaApplicationDetail.getSupplementaryDocumentsFlag());

    verify(padFileManagementService, times(1)).mapFilesToForm(form, pwaApplicationDetail, FileDocumentType.SUPPLEMENTARY_DOCUMENTS);

  }

}