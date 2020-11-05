package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.model.entity.enums.ProjectInformationQuestion;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositRadioOption;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class ProjectInformationValidatorTest {

  private ProjectInformationValidator validator;

  @Before
  public void setUp() {
    validator = new ProjectInformationValidator(new TwoFieldDateInputValidator());
  }


  @Test
  public void validate_projectName_null() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_NAME), false));
    assertThat(errorsMap).contains(
        entry("projectName", Set.of("projectName" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_projectOverview_null() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_OVERVIEW), false));
    assertThat(errorsMap).contains(
        entry("projectOverview", Set.of("projectOverview" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_methodOfPipelineDeployment_null_mandatory() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).contains(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode())));

    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.CAT_1_VARIATION, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).contains(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_methodOfPipelineDeployment_null_optional() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.CAT_2_VARIATION, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).doesNotContain(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode())));

    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.OPTIONS_VARIATION, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).doesNotContain(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  public void validate_methodOfPipelineDeployment_tooLong_optional() {
    var form = new ProjectInformationForm();
    form.setMethodOfPipelineDeployment(ValidatorTestUtils.over4000Chars());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.CAT_2_VARIATION, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).contains(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));

    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.OPTIONS_VARIATION, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).contains(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }


  @Test
  public void validate_ProposedStartNull() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE), false));
    assertThat(errorsMap).containsKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  public void validate_ProposedStartInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE), false));
    assertThat(errorsMap).containsKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  public void validate_ProposedStartValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE), false));
    assertThat(errorsMap).doesNotContainKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  public void validate_MobilisationNull() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.MOBILISATION_DATE), false));
    assertThat(errorsMap).containsKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_MobilisationInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setMobilisationDay(date.getDayOfMonth());
    form.setMobilisationMonth(date.getMonthValue());
    form.setMobilisationYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.MOBILISATION_DATE), false));
    assertThat(errorsMap).containsKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_MobilisationValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();

    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());

    form.setMobilisationDay(date.getDayOfMonth());
    form.setMobilisationMonth(date.getMonthValue());
    form.setMobilisationYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.MOBILISATION_DATE), false));
    assertThat(errorsMap).doesNotContainKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_MobilisationBeforeProposedStartDate() {

    var form = new ProjectInformationForm();
    form.setProposedStartDay(5);
    form.setProposedStartMonth(11);
    form.setProposedStartYear(2020);

    form.setMobilisationDay(4);
    form.setMobilisationMonth(11);
    form.setMobilisationYear(2020);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.MOBILISATION_DATE), false));
    assertThat(errorsMap).containsKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_EarliestCompletionNull() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_EarliestCompletionInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setEarliestCompletionDay(date.getDayOfMonth());
    form.setEarliestCompletionMonth(date.getMonthValue());
    form.setEarliestCompletionYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_EarliestCompletionValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();

    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());

    form.setEarliestCompletionDay(date.getDayOfMonth());
    form.setEarliestCompletionMonth(date.getMonthValue());
    form.setEarliestCompletionYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));
    assertThat(errorsMap).doesNotContainKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_EarliestCompletionBeforeProposedStartDate() {

    var form = new ProjectInformationForm();
    form.setProposedStartDay(5);
    form.setProposedStartMonth(11);
    form.setProposedStartYear(2020);

    form.setEarliestCompletionDay(4);
    form.setEarliestCompletionMonth(11);
    form.setEarliestCompletionYear(2020);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_LatestCompletionNull() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_LatestCompletionInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_LatestCompletionValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).doesNotContainKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_latestCompletionPastMaxFutureDate_allAppTypesExceptOptions() {

    var maxFutureDate = LocalDate.now().plusMonths(12);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(maxFutureDate.plusDays(1L).getDayOfMonth());
    form.setLatestCompletionMonth(maxFutureDate.getMonthValue());
    form.setLatestCompletionYear(maxFutureDate.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_latestCompletionPastMaxFutureDate_optionsAppType() {

    var maxFutureDate = LocalDate.now().plusMonths(6);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(maxFutureDate.plusDays(1L).getDayOfMonth());
    form.setLatestCompletionMonth(maxFutureDate.getMonthValue());
    form.setLatestCompletionYear(maxFutureDate.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.OPTIONS_VARIATION, ValidationType.FULL, Set.of(ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_EarliestAndLatestCompletionSwap() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();

    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());

    form.setEarliestCompletionDay(date.plusDays(2).getDayOfMonth());
    form.setEarliestCompletionMonth(date.plusDays(2).getMonthValue());
    form.setEarliestCompletionYear(date.plusDays(2).getYear());

    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE, ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsValues(
        Set.of("latestCompletionDay.beforeStart"),
        Set.of("latestCompletionMonth.beforeStart"),
        Set.of("latestCompletionYear.beforeStart")
    );
  }


  @Test
  public void validate_licenceTransferPlanned_noDatesProvided() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.LICENCE_TRANSFER_DATE, ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE), false));

    assertThat(errorsMap).contains(
        entry("commercialAgreementDay", Set.of("commercialAgreementDay.required")),
        entry("commercialAgreementMonth", Set.of("commercialAgreementMonth.required")),
        entry("commercialAgreementYear", Set.of("commercialAgreementYear.required")),

        entry("licenceTransferDay", Set.of("licenceTransferDay.required")),
        entry("licenceTransferMonth", Set.of("licenceTransferMonth.required")),
        entry("licenceTransferYear", Set.of("licenceTransferYear.required"))
    );

  }

  @Test
  public void validate_licenceTransferPlanned_validCommercialAgreementDate() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setCommercialAgreementDay(1);
    form.setCommercialAgreementMonth(2);
    form.setCommercialAgreementYear(2020);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL,
            ValidationType.FULL, Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE), false));

    assertThat(errorsMap).doesNotContainKeys(
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
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL,
            ValidationType.FULL, Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE), false));

    assertThat(errorsMap).contains(
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
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.LICENCE_TRANSFER_DATE), false));

    assertThat(errorsMap).doesNotContainKeys(
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
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.LICENCE_TRANSFER_DATE), false));

    assertThat(errorsMap).contains(
        entry("licenceTransferDay", Set.of("licenceTransferDay.invalid")),
        entry("licenceTransferMonth", Set.of("licenceTransferMonth.invalid")),
        entry("licenceTransferYear", Set.of("licenceTransferYear.invalid"))
    );

  }


  public Map<String, Set<String>> getErrorMap(ProjectInformationForm form,
                                              ProjectInformationFormValidationHints projectInformationFormValidationHints) {
    var errorsMap = new BeanPropertyBindingResult(form, "form");
    validator.validate(form, errorsMap, projectInformationFormValidationHints);
    return errorsMap.getFieldErrors().stream()
        .collect(
            Collectors.groupingBy(FieldError::getField, Collectors.mapping(FieldError::getCode, Collectors.toSet())));
  }

  @Test
  public void validate_permanentDepositType_noValidationRequired() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(), false));
    assertThat(errorsMap).doesNotContainKey("permanentDepositsMadeType");
  }

  @Test
  public void validate_permanentDepositType_Null() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("permanentDepositsMadeType", Set.of("permanentDepositsMadeType.notSelected"))
    );
  }

  @Test
  public void validate_permanentDepositType_LaterApp_noDate() {
    var form = new ProjectInformationForm();
    form.setPermanentDepositsMadeType(PermanentDepositRadioOption.LATER_APP);
    form.setFutureSubmissionDate(new TwoFieldDateInput());
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("futureSubmissionDate.month", Set.of("month.required")),
        entry("futureSubmissionDate.year", Set.of("year.required"))
    );
  }

  @Test
  public void validate_permanentDepositType_LaterApp_pastDate() {
    var form = new ProjectInformationForm();
    form.setPermanentDepositsMadeType(PermanentDepositRadioOption.LATER_APP);
    form.setFutureSubmissionDate(new TwoFieldDateInput(2020, 2));
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("futureSubmissionDate.month", Set.of("month.afterDate")),
        entry("futureSubmissionDate.year", Set.of("year.afterDate"))
    );
  }

  @Test
  public void validate_temporaryDeposit_noDescription() {
    var form = new ProjectInformationForm();
    form.setTemporaryDepositsMade(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("temporaryDepDescription", Set.of("temporaryDepDescription.empty"))
    );
  }


  @Test
  public void validate_temporaryDeposit_Null() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("temporaryDepositsMade", Set.of("temporaryDepositsMade.notSelected"))
    );
  }


  @Test
  public void validate_noFdpQuestionRequired() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(), false));
    assertThat(errorsMap).doesNotContain(
        entry("fdpOptionSelected", Set.of("fdpOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("fdpConfirmationFlag", Set.of("fdpConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("fdpNotSelectedReason", Set.of("fdpNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_fdpQuestionRequired_valid() {
    var form = new ProjectInformationForm();
    form.setFdpOptionSelected(true);
    form.setFdpConfirmationFlag(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN), true));
    assertThat(errorsMap).doesNotContain(
        entry("fdpOptionSelected", Set.of("fdpOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("fdpConfirmationFlag", Set.of("fdpConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("fdpNotSelectedReason", Set.of("fdpNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_fdpQuestionRequired_noFdpOptionSelected() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN), true));
    assertThat(errorsMap).contains(
        entry("fdpOptionSelected", Set.of("fdpOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_fdpQuestionRequired_fdpOptionSelected_fdpConfirmationFlagNotChecked() {
    var form = new ProjectInformationForm();
    form.setFdpOptionSelected(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN), true));
    assertThat(errorsMap).contains(
        entry("fdpConfirmationFlag", Set.of("fdpConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_fdpQuestionRequired_fdpOptionSelectedIsNo_fdpNotSelectedReasonEmpty() {
    var form = new ProjectInformationForm();
    form.setFdpOptionSelected(false);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN), true));
    assertThat(errorsMap).contains(
        entry("fdpNotSelectedReason", Set.of("fdpNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  public void validate_oneProjectLayoutDiagramFile() {
    var form = new ProjectInformationForm();
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm("1", "2", Instant.now())
    ));
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM), false));
    assertThat(errorsMap).doesNotContainKeys("uploadedFileWithDescriptionForms");
  }

  @Test
  public void validate_tooManyProjectLayoutDiagramFiles() {
    var form = new ProjectInformationForm();
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm("1", "2", Instant.now()),
        new UploadFileWithDescriptionForm("3", "4", Instant.now())
    ));
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM), false));
    assertThat(errorsMap).containsKeys("uploadedFileWithDescriptionForms");
  }

  @Test
  public void validate_partialValidation_noFullValidationErrorsPresent() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.PARTIAL, EnumSet.allOf(ProjectInformationQuestion.class), false));
    assertThat(errorsMap).containsOnlyKeys("projectName", "projectOverview", "methodOfPipelineDeployment");
  }

  @Test
  public void validate_validationNotRequired_whenQuestionNotProvided() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, ValidationType.FULL, Set.of(), false));
    assertThat(errorsMap).isEmpty();
  }

}