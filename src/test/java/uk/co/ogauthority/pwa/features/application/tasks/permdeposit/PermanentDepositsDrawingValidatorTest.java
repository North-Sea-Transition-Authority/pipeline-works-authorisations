package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@ExtendWith(MockitoExtension.class)
public class PermanentDepositsDrawingValidatorTest {

  private PermanentDepositsDrawingValidator validator;

  @Mock
  private DepositDrawingsService service;

  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    validator = new PermanentDepositsDrawingValidator();
    pwaApplicationDetail = new PwaApplicationDetail();
  }

  public Map<String, Set<String>> getErrorMap(PermanentDepositDrawingForm form) {
    var errors = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, errors, service, pwaApplicationDetail);
    return errors.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
  }

  @Test
  void validate_form_empty() {
    var form = new PermanentDepositDrawingForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("reference", Set.of("reference.required")),
        entry("uploadedFiles", Set.of(FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE)),
        entry("selectedDeposits", Set.of("selectedDeposits.required"))
    );
  }

  @Test
  void validate_ref_notUnique() {
    var form = new PermanentDepositDrawingForm();
    form.setReference("existing ref");
    when(service.isDrawingReferenceUnique(
        form.getReference(), null, pwaApplicationDetail)).thenReturn(false);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("reference", Set.of("reference.required"))
    );
  }

  @Test
  void validate_ref_valid() {
    var form = new PermanentDepositDrawingForm();
    form.setReference("new ref");
    when(service.isDrawingReferenceUnique(
        form.getReference(), null, pwaApplicationDetail)).thenReturn(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).doesNotContain((entry("reference", Set.of("reference.required"))));
  }

  @Test
  void validate_ref_existingDrawing_valid() {
    var form = new PermanentDepositDrawingForm();
    form.setReference("new ref");
    when(service.isDrawingReferenceUnique(
        form.getReference(), 1, pwaApplicationDetail)).thenReturn(true);

    var errors = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, errors, service, pwaApplicationDetail, 1);
    Map<String, Set<String>> errorsMap =  errors.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
    assertThat(errorsMap).doesNotContain((entry("reference", Set.of("reference.required"))));
  }

  @Test
  void validate_files_moreThanOneUploaded() {
    var form = new PermanentDepositDrawingForm();
    form.setUploadedFiles(
        List.of(
            FileManagementValidatorTestUtils.createUploadedFileForm(),
            FileManagementValidatorTestUtils.createUploadedFileForm()
        )
    );

    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry(
        "uploadedFiles",
        Set.of(FileValidationUtils.ABOVE_LIMIT_ERROR_CODE.formatted("uploadedFiles"))));
  }

  @Test
  void validate_drawingReferenceSurpassMaxLimit_invalid() {
    var form = new PermanentDepositDrawingForm();
    form.setReference(StringUtils.repeat("b", 151));
    when(service.isDrawingReferenceUnique(
        form.getReference(), 1, pwaApplicationDetail)).thenReturn(true);

    var errors = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, errors, service, pwaApplicationDetail, 1);
    Map<String, Set<String>> errorsMap =  errors.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));

    assertThat(errorsMap).contains(entry("reference", Set.of("reference.maxLengthExceeded")));
  }

  @Test
  void validate_depositSelectionEmpty() {
    var form = new PermanentDepositDrawingForm();
    form.setSelectedDeposits(Set.of());
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("selectedDeposits", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("selectedDeposits"))));
  }
}