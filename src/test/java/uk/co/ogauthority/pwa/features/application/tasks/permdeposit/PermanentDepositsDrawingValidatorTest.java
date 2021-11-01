package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositsDrawingValidatorTest {

  private PermanentDepositsDrawingValidator validator;

  @Mock
  private DepositDrawingsService service;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
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
  public void validate_form_empty() {
    var form = new PermanentDepositDrawingForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("reference", Set.of("reference.required")),
        entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        entry("selectedDeposits", Set.of("selectedDeposits.required"))
    );
  }

  @Test
  public void validate_ref_notUnique() {
    var form = new PermanentDepositDrawingForm();
    form.setReference("existing ref");
    when(service.isDrawingReferenceUnique(
        form.getReference(), null, pwaApplicationDetail)).thenReturn(false);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("reference", Set.of("reference.required"))
    );
  }

  @Test
  public void validate_ref_valid() {
    var form = new PermanentDepositDrawingForm();
    form.setReference("new ref");
    when(service.isDrawingReferenceUnique(
        form.getReference(), null, pwaApplicationDetail)).thenReturn(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).doesNotContain((entry("reference", Set.of("reference.required"))));
  }

  @Test
  public void validate_ref_existingDrawing_valid() {
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
  public void validate_files_moreThanOneUploaded() {
    var form = new PermanentDepositDrawingForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm(), new UploadFileWithDescriptionForm()));
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  public void validate_depositSelectionEmpty() {
    var form = new PermanentDepositDrawingForm();
    form.setSelectedDeposits(Set.of());
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("selectedDeposits", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("selectedDeposits"))));
  }





}