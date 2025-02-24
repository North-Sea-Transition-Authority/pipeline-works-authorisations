package uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.fileupload.FileUploadTestUtil;

@ExtendWith(MockitoExtension.class)
class SupplementaryDocumentsServiceTest {

  @Mock
  private PadFileService padFileService;

  @Mock
  private PwaApplicationDetailService detailService;

  private SupplementaryDocumentsService supplementaryDocumentsService;

  private PwaApplicationDetail pwaApplicationDetail;
  private SupplementaryDocumentsForm form;

  @BeforeEach
  void setUp() {

    supplementaryDocumentsService = new SupplementaryDocumentsService(padFileService, detailService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    form = new SupplementaryDocumentsForm();

  }

  @Test
  void isComplete() {

    supplementaryDocumentsService.isComplete(pwaApplicationDetail);
    verify(padFileService, times(1)).mapFilesToForm(any(), eq(pwaApplicationDetail), eq(ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS));

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
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "2", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_full_documentDescriptionOverMaxCharLength_invalid() {
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    var fieldErrors = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(fieldErrors).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
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
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "2", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  void validate_partial_documentDescriptionOverMaxCharLength_invalid() {
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    var fieldErrors = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(fieldErrors).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
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

    verify(padFileService, times(1)).mapFilesToForm(form, pwaApplicationDetail, ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS);

  }

}