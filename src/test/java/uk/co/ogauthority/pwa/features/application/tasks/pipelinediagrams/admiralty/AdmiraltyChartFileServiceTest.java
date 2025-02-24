package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.fileupload.FileUploadTestUtil;

@ExtendWith(MockitoExtension.class)
class AdmiraltyChartFileServiceTest {

  @Mock
  private PadFileService padFileService;

  private final SpringValidatorAdapter springValidatorAdapter = new SpringValidatorAdapter(
      Validation.buildDefaultValidatorFactory().getValidator());

  private AdmiraltyChartFileService admiraltyChartFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private final AdmiraltyChartDocumentForm form = new AdmiraltyChartDocumentForm();

  @BeforeEach
  void setUp() {

    admiraltyChartFileService = new AdmiraltyChartFileService(padFileService, springValidatorAdapter);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  void validate_full_whenNoDocumentRequired_andDocumentProvidedWithDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "2", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_full_whenNoDocumentRequired_andDocumentProvidedWithoutDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
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
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_partial_whenDocumentWithoutDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  void validate_partial_whenDocumentWithDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_partial_whenDocumentDescriptionOverMaxCharLength() {
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    var fieldErrors = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(fieldErrors).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  void validate_full_whenDocumentDescriptionOverMaxCharLength() {
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    admiraltyChartFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    var fieldErrors = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(fieldErrors).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  void getAdmiraltyChartFile_present() {

    var fullPadFile = new PadFile();
    fullPadFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);

    when(padFileService.getAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART))
        .thenReturn(List.of(fullPadFile));

    assertThat(admiraltyChartFileService.getAdmiraltyChartFile(pwaApplicationDetail)).contains(fullPadFile);

  }

  @Test
  void getAdmiraltyChartFile_notFull_empty() {

    var tempPadFile = new PadFile();
    tempPadFile.setFileLinkStatus(ApplicationFileLinkStatus.TEMPORARY);

    when(padFileService.getAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART))
        .thenReturn(List.of(tempPadFile));

    assertThat(admiraltyChartFileService.getAdmiraltyChartFile(pwaApplicationDetail)).isEmpty();

  }

  @Test
  void getAdmiraltyChartFile_no_empty() {

    when(padFileService.getAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART))
        .thenReturn(List.of());

    assertThat(admiraltyChartFileService.getAdmiraltyChartFile(pwaApplicationDetail)).isEmpty();

  }

}