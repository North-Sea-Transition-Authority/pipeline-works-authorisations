package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositRadioOption;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProjectInformationValidatorTest {

  private ProjectInformationValidator validator;
  private ProjectInformationFormValidationHints projectInformationFormValidationHints;

  @Before
  public void setUp() {
    validator = new ProjectInformationValidator();
    projectInformationFormValidationHints = new ProjectInformationFormValidationHints(false, false);
  }

  @Test
  public void validate_ProposedStartNull() {
    var form = new ProjectInformationForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).containsKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  public void validate_ProposedStartInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).containsKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  public void validate_ProposedStartValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).doesNotContainKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  public void validate_MobilisationNull() {
    var form = new ProjectInformationForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).containsKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_MobilisationInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setMobilisationDay(date.getDayOfMonth());
    form.setMobilisationMonth(date.getMonthValue());
    form.setMobilisationYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).containsKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_MobilisationValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setMobilisationDay(date.getDayOfMonth());
    form.setMobilisationMonth(date.getMonthValue());
    form.setMobilisationYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).doesNotContainKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_EarliestCompletionNull() {
    var form = new ProjectInformationForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).containsKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_EarliestCompletionInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setEarliestCompletionDay(date.getDayOfMonth());
    form.setEarliestCompletionMonth(date.getMonthValue());
    form.setEarliestCompletionYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).containsKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_EarliestCompletionValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setEarliestCompletionDay(date.getDayOfMonth());
    form.setEarliestCompletionMonth(date.getMonthValue());
    form.setEarliestCompletionYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).doesNotContainKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_LatestCompletionNull() {
    var form = new ProjectInformationForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_LatestCompletionInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_LatestCompletionValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).doesNotContainKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_EarliestAndLatestCompletionSwap() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();

    form.setEarliestCompletionDay(date.plusDays(2).getDayOfMonth());
    form.setEarliestCompletionMonth(date.plusDays(2).getMonthValue());
    form.setEarliestCompletionYear(date.plusDays(2).getYear());

    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);
    assertThat(errors).containsValues(
            Set.of("latestCompletionDay.beforeStart"),
            Set.of("latestCompletionMonth.beforeStart"),
            Set.of("latestCompletionYear.beforeStart")
    );
  }


  @Test
  public void validate_licenceTransferPlanned_noDatesProvided() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);

    assertThat(errors).contains(
            entry("commercialAgreementDay", Set.of("commercialAgreementDay.invalid")),
            entry("commercialAgreementMonth", Set.of("commercialAgreementMonth.invalid")),
            entry("commercialAgreementYear", Set.of("commercialAgreementYear.invalid")),

            entry("licenceTransferDay", Set.of("licenceTransferDay.invalid")),
            entry("licenceTransferMonth", Set.of("licenceTransferMonth.invalid")),
            entry("licenceTransferYear", Set.of("licenceTransferYear.invalid"))
    );

  }

  @Test
  public void validate_licenceTransferPlanned_validCommercialAgreementDate() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setCommercialAgreementDay(1);
    form.setCommercialAgreementMonth(2);
    form.setCommercialAgreementYear(2020);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);

    assertThat(errors).doesNotContainKeys(
            "commercialAgreementDay",
            "commercialAgreementMonth",
            "commercialAgreementYear"
    );

  }

  @Test
  public void validate_licenceTransferPlanned_invalidCommercialAgreementDate() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setCommercialAgreementDay(100);
    form.setCommercialAgreementMonth(100);
    form.setCommercialAgreementYear(2020);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);

    assertThat(errors).contains(
            entry("commercialAgreementDay", Set.of("commercialAgreementDay.invalid")),
            entry("commercialAgreementMonth", Set.of("commercialAgreementMonth.invalid")),
            entry("commercialAgreementYear", Set.of("commercialAgreementYear.invalid"))
    );

  }

  @Test
  public void validate_licenceTransferPlanned_validLicenceTransferDate() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setLicenceTransferDay(1);
    form.setLicenceTransferMonth(2);
    form.setLicenceTransferYear(2020);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);

    assertThat(errors).doesNotContainKeys(
            "licenceTransferDay",
            "licenceTransferMonth",
            "licenceTransferYear"
    );

  }

  @Test
  public void validate_licenceTransferPlanned_invalidLicenceTransferDate() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setLicenceTransferDay(100);
    form.setLicenceTransferMonth(100);
    form.setLicenceTransferYear(2020);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, projectInformationFormValidationHints);

    assertThat(errors).contains(
            entry("licenceTransferDay", Set.of("licenceTransferDay.invalid")),
            entry("licenceTransferMonth", Set.of("licenceTransferMonth.invalid")),
            entry("licenceTransferYear", Set.of("licenceTransferYear.invalid"))
    );

  }


  public Map<String, Set<String>> getErrorMap(ProjectInformationForm form, ProjectInformationFormValidationHints projectInformationFormValidationHints) {
    var errors = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, errors, projectInformationFormValidationHints);
    return errors.getFieldErrors().stream()
            .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
  }

  @Test
  public void validate_permanentDepositType_noValidationRequired() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(false, false));
    assertThat(errorsMap).doesNotContainKey("permanentDepositsMadeType");
  }

  @Test
  public void validate_permanentDepositType_Null() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(true, true));
    assertThat(errorsMap).contains(
            entry("permanentDepositsMadeType", Set.of("permanentDepositsMadeType.notSelected"))
    );
  }

  @Test
  public void validate_permanentDepositType_LaterApp_noDate() {
    var form = new ProjectInformationForm();
    form.setPermanentDepositsMadeType(PermanentDepositRadioOption.LATER_APP);
    Map<String, Set<String>> errorsMap = getErrorMap(form,  new ProjectInformationFormValidationHints(true, true));
    assertThat(errorsMap).contains(
            entry("futureAppSubmissionMonth", Set.of("futureAppSubmissionMonth.invalid")),
            entry("futureAppSubmissionYear", Set.of("futureAppSubmissionYear.invalid"))
    );
  }

  @Test
  public void validate_permanentDepositType_LaterApp_pastDate() {
    var form = new ProjectInformationForm();
    form.setPermanentDepositsMadeType(PermanentDepositRadioOption.LATER_APP);
    form.setFutureAppSubmissionMonth(2);
    form.setFutureAppSubmissionYear(2020);
    Map<String, Set<String>> errorsMap = getErrorMap(form,  new ProjectInformationFormValidationHints(true, true));
    assertThat(errorsMap).contains(
            entry("futureAppSubmissionMonth", Set.of("futureAppSubmissionMonth.beforeToday")),
            entry("futureAppSubmissionYear", Set.of("futureAppSubmissionYear.beforeToday"))
    );
  }

  @Test
  public void validate_temporaryDeposit_noDescription() {
    var form = new ProjectInformationForm();
    form.setTemporaryDepositsMade(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form,  new ProjectInformationFormValidationHints(true, false));
    assertThat(errorsMap).contains(
            entry("temporaryDepDescription", Set.of("temporaryDepDescription.empty"))
    );
  }


  @Test
  public void validate_temporaryDeposit_Null() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form,  new ProjectInformationFormValidationHints(true, false));
    assertThat(errorsMap).contains(
            entry("temporaryDepositsMade", Set.of("temporaryDepositsMade.notSelected"))
    );
  }

}