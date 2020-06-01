package uk.co.ogauthority.pwa.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositDrawingValidatorTest {

  private PermanentDepositsDrawingValidator validator;

  @Mock
  private DepositDrawingsService service;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    validator = new PermanentDepositsDrawingValidator();
    pwaApplicationDetail = new PwaApplicationDetail();
  }


  public Map<String, Set<String>> getErrorMap(PermanentDepositDrawingsForm form) {
    var errors = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, errors, service, pwaApplicationDetail);
    return errors.getFieldErrors().stream()
        .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
  }


  @Test
  public void validate_form_empty() {
    var form = new PermanentDepositDrawingsForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(
        entry("reference", Set.of("reference.required")),
        entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())),
        entry("selectedDeposits", Set.of("selectedDeposits.required"))
    );
  }

  @Test
  public void validate_ref_notUnique() {
    var form = new PermanentDepositDrawingsForm();
    form.setReference("existing ref");
    when(service.isDrawingReferenceUnique(
        form.getReference(), pwaApplicationDetail)).thenReturn(false);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("reference", Set.of("reference.required"))
    );
  }

  @Test
  public void validate_ref_valid() {
    var form = new PermanentDepositDrawingsForm();
    form.setReference("new ref");
    when(service.isDrawingReferenceUnique(
        form.getReference(), pwaApplicationDetail)).thenReturn(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).doesNotContain((entry("reference", Set.of("reference.required"))));
  }

  @Test
  public void validate_files_moreThanOneUploaded() {
    var form = new PermanentDepositDrawingsForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm(), new UploadFileWithDescriptionForm()));
    Map<String, Set<String>> errorsMap = getErrorMap(form);
    assertThat(errorsMap).contains(entry("uploadedFileWithDescriptionForms", Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }





}