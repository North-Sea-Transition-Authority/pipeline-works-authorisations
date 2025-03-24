package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransaction;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransactionService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@ExtendWith(MockitoExtension.class)
public class ProjectInformationValidatorTest {

  private ProjectInformationValidator validator;

  private Set<ProjectInformationQuestion> partialDateValidationQuestions;

  @Mock
  private PearsLicenceTransactionService pearsLicenceTransactionService;

  @BeforeEach
  void setUp() {
    validator = new ProjectInformationValidator(new TwoFieldDateInputValidator(), pearsLicenceTransactionService);

    partialDateValidationQuestions = Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
        ProjectInformationQuestion.MOBILISATION_DATE,
        ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
        ProjectInformationQuestion.LATEST_COMPLETION_DATE,
        ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED,
        ProjectInformationQuestion.LICENCE_TRANSFER_DATE,
        ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE,
        ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE);
  }


  @Test
  void validate_projectName_null() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_NAME), false));
    assertThat(errorsMap).contains(
        entry("projectName", Set.of("projectName" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_projectName_tooBig() {
    var form = new ProjectInformationForm();
    form.setProjectName(StringUtils.repeat("a", 151));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator,
        form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROJECT_NAME), false)
    );

    assertThat(errorsMap).contains(
        entry("projectName", Set.of("projectName" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  @Test
  void validate_projectName_rightSize() {
    var form = new ProjectInformationForm();
    form.setProjectName(StringUtils.repeat("a", 150));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(
        validator,
        form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROJECT_NAME), false)
    );

    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_projectOverview_null() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_OVERVIEW), false));
    assertThat(errorsMap).contains(
        entry("projectOverview", Set.of("projectOverview" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_methodOfPipelineDeployment_null_mandatory() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).contains(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode())));

    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).contains(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_methodOfPipelineDeployment_null_optional() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.CAT_2_VARIATION, PwaResourceType.PETROLEUM,ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).doesNotContain(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode())));

    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.OPTIONS_VARIATION, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).doesNotContain(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_methodOfPipelineDeployment_tooLong_optional() {
    var form = new ProjectInformationForm();
    form.setMethodOfPipelineDeployment(ValidatorTestUtils.overMaxDefaultCharLength());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.CAT_2_VARIATION, PwaResourceType.PETROLEUM,ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).contains(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));

    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.OPTIONS_VARIATION, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT), false));
    assertThat(errorsMap).contains(
        entry("methodOfPipelineDeployment", Set.of("methodOfPipelineDeployment" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode())));
  }

  private void setYearOnFormPartialDateQuestions(ProjectInformationForm form, int year) {
    form.setProposedStartYear(year);
    form.setMobilisationYear(year);
    form.setEarliestCompletionYear(year);
    form.setLatestCompletionYear(year);
    form.setLicenceTransferPlanned(true);
    form.setLicenceTransferYear(year);
    form.setCommercialAgreementYear(year);
    form.setPermanentDepositsMadeType(PermanentDepositMade.LATER_APP);
    form.setFutureSubmissionDate(new TwoFieldDateInput(year, 1));
  }

  @Test
  void validate_partialDates_yearTooBig() {
    var form = new ProjectInformationForm();
    int invalidLargeYear = 4001;
    setYearOnFormPartialDateQuestions(form, invalidLargeYear);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.PARTIAL,
            partialDateValidationQuestions,
            false));

    assertThat(errorsMap).contains(
        entry("proposedStartDay", Set.of("proposedStartDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("proposedStartMonth", Set.of("proposedStartMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("proposedStartYear", Set.of("proposedStartYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("mobilisationDay", Set.of("mobilisationDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("mobilisationMonth", Set.of("mobilisationMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("mobilisationYear", Set.of("mobilisationYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("earliestCompletionDay", Set.of("earliestCompletionDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("earliestCompletionMonth", Set.of("earliestCompletionMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("earliestCompletionYear", Set.of("earliestCompletionYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("latestCompletionDay", Set.of("latestCompletionDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("latestCompletionMonth", Set.of("latestCompletionMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("latestCompletionYear", Set.of("latestCompletionYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("licenceTransferDay", Set.of("licenceTransferDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("licenceTransferMonth", Set.of("licenceTransferMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("licenceTransferYear", Set.of("licenceTransferYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("commercialAgreementDay", Set.of("commercialAgreementDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("commercialAgreementMonth", Set.of("commercialAgreementMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("commercialAgreementYear", Set.of("commercialAgreementYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("futureSubmissionDate.month", Set.of("month" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("futureSubmissionDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  void validate_partialDates_yearTooSmall() {
    var form = new ProjectInformationForm();
    int invalidSmallYear = 999;
    setYearOnFormPartialDateQuestions(form, invalidSmallYear);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.PARTIAL,
            partialDateValidationQuestions,
            false));

    assertThat(errorsMap).contains(
        entry("proposedStartDay", Set.of("proposedStartDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("proposedStartMonth", Set.of("proposedStartMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("proposedStartYear", Set.of("proposedStartYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("mobilisationDay", Set.of("mobilisationDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("mobilisationMonth", Set.of("mobilisationMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("mobilisationYear", Set.of("mobilisationYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("earliestCompletionDay", Set.of("earliestCompletionDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("earliestCompletionMonth", Set.of("earliestCompletionMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("earliestCompletionYear", Set.of("earliestCompletionYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("latestCompletionDay", Set.of("latestCompletionDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("latestCompletionMonth", Set.of("latestCompletionMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("latestCompletionYear", Set.of("latestCompletionYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("licenceTransferDay", Set.of("licenceTransferDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("licenceTransferMonth", Set.of("licenceTransferMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("licenceTransferYear", Set.of("licenceTransferYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("commercialAgreementDay", Set.of("commercialAgreementDay" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("commercialAgreementMonth", Set.of("commercialAgreementMonth" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("commercialAgreementYear", Set.of("commercialAgreementYear" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("futureSubmissionDate.month", Set.of("month" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("futureSubmissionDate.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())));
  }


  @Test
  void validate_ProposedStartNull() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE), false));
    assertThat(errorsMap).containsKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  void validate_ProposedStartInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE), false));
    assertThat(errorsMap).containsKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  void validate_ProposedStartValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE), false));
    assertThat(errorsMap).doesNotContainKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  void validate_MobilisationNull() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.MOBILISATION_DATE), false));
    assertThat(errorsMap).containsKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  void validate_MobilisationInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setMobilisationDay(date.getDayOfMonth());
    form.setMobilisationMonth(date.getMonthValue());
    form.setMobilisationYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.MOBILISATION_DATE), false));
    assertThat(errorsMap).containsKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  void validate_MobilisationValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();

    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());

    form.setMobilisationDay(date.getDayOfMonth());
    form.setMobilisationMonth(date.getMonthValue());
    form.setMobilisationYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.MOBILISATION_DATE), false));
    assertThat(errorsMap).doesNotContainKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  void validate_MobilisationBeforeProposedStartDate_ok() {

    var form = new ProjectInformationForm();
    form.setProposedStartDay(5);
    form.setProposedStartMonth(11);
    form.setProposedStartYear(3020);

    form.setMobilisationDay(4);
    form.setMobilisationMonth(11);
    form.setMobilisationYear(3020);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, new ProjectInformationFormValidationHints(
            PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE, ProjectInformationQuestion.MOBILISATION_DATE), false));

    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_MobilisationOnProposedStartDate_ok() {

    var form = new ProjectInformationForm();
    form.setProposedStartDay(5);
    form.setProposedStartMonth(11);
    form.setProposedStartYear(3020);

    form.setMobilisationDay(5);
    form.setMobilisationMonth(11);
    form.setMobilisationYear(3020);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE, ProjectInformationQuestion.MOBILISATION_DATE), false));

    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_MobilisationAfterProposedStartDate_invalid() {

    var form = new ProjectInformationForm();
    form.setProposedStartDay(5);
    form.setProposedStartMonth(11);
    form.setProposedStartYear(3020);

    form.setMobilisationDay(6);
    form.setMobilisationMonth(11);
    form.setMobilisationYear(3020);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE, ProjectInformationQuestion.MOBILISATION_DATE), false));

    assertThat(errorsMap).contains(
        entry("mobilisationDay", Set.of("mobilisationDay" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())),
        entry("mobilisationMonth", Set.of("mobilisationMonth" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())),
        entry("mobilisationYear", Set.of("mobilisationYear" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));

  }

  @Test
  void validate_EarliestCompletionNull() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  void validate_EarliestCompletionInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setEarliestCompletionDay(date.getDayOfMonth());
    form.setEarliestCompletionMonth(date.getMonthValue());
    form.setEarliestCompletionYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  void validate_EarliestCompletionValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();

    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());

    form.setEarliestCompletionDay(date.getDayOfMonth());
    form.setEarliestCompletionMonth(date.getMonthValue());
    form.setEarliestCompletionYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));
    assertThat(errorsMap).doesNotContainKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  void validate_EarliestCompletionBeforeProposedStartDate_invalid() {

    var form = new ProjectInformationForm();
    form.setProposedStartDay(7);
    form.setProposedStartMonth(11);
    form.setProposedStartYear(3020);

    form.setEarliestCompletionDay(6);
    form.setEarliestCompletionMonth(11);
    form.setEarliestCompletionYear(3020);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE, ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));
    assertThat(errorsMap).contains(
        entry("earliestCompletionDay", Set.of("earliestCompletionDay" + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode())),
        entry("earliestCompletionMonth", Set.of("earliestCompletionMonth" + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode())),
        entry("earliestCompletionYear", Set.of("earliestCompletionYear" + FieldValidationErrorCodes.BEFORE_SOME_DATE.getCode())));
  }

  @Test
  void validate_EarliestCompletionOnProposedStartDate_ok() {

    var form = new ProjectInformationForm();
    form.setProposedStartDay(7);
    form.setProposedStartMonth(11);
    form.setProposedStartYear(3020);

    form.setEarliestCompletionDay(7);
    form.setEarliestCompletionMonth(11);
    form.setEarliestCompletionYear(3020);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE, ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));

    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_EarliestCompletionAfterProposedStartDate_ok() {

    var form = new ProjectInformationForm();
    form.setProposedStartDay(7);
    form.setProposedStartMonth(11);
    form.setProposedStartYear(3020);

    form.setEarliestCompletionDay(8);
    form.setEarliestCompletionMonth(11);
    form.setEarliestCompletionYear(3020);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROPOSED_START_DATE, ProjectInformationQuestion.EARLIEST_COMPLETION_DATE), false));

    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_LatestCompletionNull() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  void validate_LatestCompletionInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  void validate_LatestCompletionValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).doesNotContainKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  void validate_latestCompletionPastMaxFutureDate_NonExtendableAppTypes_invalid() {

    var form = new ProjectInformationForm();
    var proposedStartDate = LocalDate.now().plusDays(5);
    var earliestCompletionDate = proposedStartDate.plusDays(1);
    form.setProposedStartDay(proposedStartDate.getDayOfMonth());
    form.setProposedStartMonth(proposedStartDate.getMonthValue());
    form.setProposedStartYear(proposedStartDate.getYear());
    form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
    form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
    form.setEarliestCompletionYear(earliestCompletionDate.getYear());

    var maxFutureDate = proposedStartDate.plusMonths(12);
    var nextDayOfMonth = maxFutureDate.plusDays(1L).getDayOfMonth();
    form.setLatestCompletionDay(nextDayOfMonth);

    var month = maxFutureDate.getMonthValue();
    form.setLatestCompletionMonth(nextDayOfMonth == 1 ? month + 1 : month);
    form.setLatestCompletionYear(maxFutureDate.getYear());

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.DECOMMISSIONING,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));

    assertThat(errorsMap).contains(
        entry("latestCompletionDay", Set.of("latestCompletionDay" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())),
        entry("latestCompletionMonth", Set.of("latestCompletionMonth" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())),
        entry("latestCompletionYear", Set.of("latestCompletionYear" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));

  }

  @Test
  void validate_latestCompletionPastMaxFutureDate_ExtendableAppTypes_valid() {

    var form = new ProjectInformationForm();
    var proposedStartDate = LocalDate.now().plusDays(5);
    var earliestCompletionDate = proposedStartDate.plusDays(1);
    form.setProposedStartDay(proposedStartDate.getDayOfMonth());
    form.setProposedStartMonth(proposedStartDate.getMonthValue());
    form.setProposedStartYear(proposedStartDate.getYear());
    form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
    form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
    form.setEarliestCompletionYear(earliestCompletionDate.getYear());

    var maxFutureDate = proposedStartDate.plusMonths(12);
    var nextDayOfMonth = maxFutureDate.plusDays(1L).getDayOfMonth();
    form.setLatestCompletionDay(nextDayOfMonth);

    var month = maxFutureDate.getMonthValue();
    form.setLatestCompletionMonth(nextDayOfMonth == 1 ? month + 1 : month);
    form.setLatestCompletionYear(maxFutureDate.getYear());

    var expectedErrorMap = new HashMap<String, Set<String>>();
    expectedErrorMap.put("latestCompletionDay", Set.of("latestCompletionDay.afterDate"));
    expectedErrorMap.put("latestCompletionMonth", Set.of("latestCompletionMonth.afterDate"));
    expectedErrorMap.put("latestCompletionYear", Set.of("latestCompletionYear.afterDate"));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.INITIAL,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).isEmpty();
    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.CAT_1_VARIATION,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).isEmpty();
    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.CAT_2_VARIATION,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).isEqualTo(expectedErrorMap);
    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.OPTIONS_VARIATION,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).isEqualTo(expectedErrorMap);
    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.HUOO_VARIATION,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).isEqualTo(expectedErrorMap);
    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.DEPOSIT_CONSENT,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).isEqualTo(expectedErrorMap);
    errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.DECOMMISSIONING,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).isEqualTo(expectedErrorMap);
  }

  @Test
  void validate_latestCompletionOnMaxFutureDate_allAppTypesExceptOptions_ok() {

    var form = new ProjectInformationForm();
    var proposedStartDate = LocalDate.now().plusDays(5);
    var earliestCompletionDate = proposedStartDate.plusDays(1);
    form.setProposedStartDay(proposedStartDate.getDayOfMonth());
    form.setProposedStartMonth(proposedStartDate.getMonthValue());
    form.setProposedStartYear(proposedStartDate.getYear());
    form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
    form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
    form.setEarliestCompletionYear(earliestCompletionDate.getYear());

    var maxFutureDate = proposedStartDate.plusMonths(12);
    form.setLatestCompletionDay(maxFutureDate.getDayOfMonth());
    form.setLatestCompletionMonth(maxFutureDate.getMonthValue());
    form.setLatestCompletionYear(maxFutureDate.getYear());

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.INITIAL,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));

    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_latestCompletionBeforeMaxFutureDate_allAppTypesExceptOptions_ok() {

    var form = new ProjectInformationForm();
    var proposedStartDate = LocalDate.now().plusDays(5);
    var earliestCompletionDate = proposedStartDate.plusDays(1);
    form.setProposedStartDay(proposedStartDate.getDayOfMonth());
    form.setProposedStartMonth(proposedStartDate.getMonthValue());
    form.setProposedStartYear(proposedStartDate.getYear());
    form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
    form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
    form.setEarliestCompletionYear(earliestCompletionDate.getYear());

    var maxFutureDate = proposedStartDate.plusMonths(12);
    form.setLatestCompletionDay(Math.max(maxFutureDate.getDayOfMonth() - 1, 1));
    form.setLatestCompletionMonth(maxFutureDate.getMonthValue());
    form.setLatestCompletionYear(maxFutureDate.getYear());

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.INITIAL,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));

    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_latestCompletionPastMaxFutureDate_optionsAppType_invalid() {

    var form = new ProjectInformationForm();
    var proposedStartDate = LocalDate.now().plusDays(5);
    var earliestCompletionDate = proposedStartDate.plusDays(1);
    form.setProposedStartDay(proposedStartDate.getDayOfMonth());
    form.setProposedStartMonth(proposedStartDate.getMonthValue());
    form.setProposedStartYear(proposedStartDate.getYear());
    form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
    form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
    form.setEarliestCompletionYear(earliestCompletionDate.getYear());

    var maxFutureDate = proposedStartDate.plusMonths(6).plusDays(1L);
    form.setLatestCompletionDay(maxFutureDate.getDayOfMonth());
    form.setLatestCompletionMonth(maxFutureDate.getMonthValue());
    form.setLatestCompletionYear(maxFutureDate.getYear());

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.OPTIONS_VARIATION,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));

    assertThat(errorsMap).contains(
        entry("latestCompletionDay", Set.of("latestCompletionDay" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())),
        entry("latestCompletionMonth", Set.of("latestCompletionMonth" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())),
        entry("latestCompletionYear", Set.of("latestCompletionYear" + FieldValidationErrorCodes.AFTER_SOME_DATE.getCode())));

  }

  @Test
  void validate_latestCompletionOnMaxFutureDate_optionsAppType_ok() {

    var form = new ProjectInformationForm();
    var proposedStartDate = LocalDate.now().plusDays(5);
    var earliestCompletionDate = proposedStartDate.plusDays(1);
    form.setProposedStartDay(proposedStartDate.getDayOfMonth());
    form.setProposedStartMonth(proposedStartDate.getMonthValue());
    form.setProposedStartYear(proposedStartDate.getYear());
    form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
    form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
    form.setEarliestCompletionYear(earliestCompletionDate.getYear());

    var maxFutureDate = proposedStartDate.plusMonths(6).minusDays(1);
    form.setLatestCompletionDay(maxFutureDate.getDayOfMonth());
    form.setLatestCompletionMonth(maxFutureDate.getMonthValue());
    form.setLatestCompletionYear(maxFutureDate.getYear());

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.OPTIONS_VARIATION,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));

    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_latestCompletionBeforeMaxFutureDate_optionsAppType_ok() {

    var form = new ProjectInformationForm();
    var proposedStartDate = LocalDate.now().plusDays(1);
    var earliestCompletionDate = proposedStartDate.plusDays(1);
    form.setProposedStartDay(proposedStartDate.getDayOfMonth());
    form.setProposedStartMonth(proposedStartDate.getMonthValue());
    form.setProposedStartYear(proposedStartDate.getYear());
    form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
    form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
    form.setEarliestCompletionYear(earliestCompletionDate.getYear());

    var maxFutureDate = proposedStartDate.plusMonths(5);
    form.setLatestCompletionDay(Math.max(maxFutureDate.getDayOfMonth() - 1, 1));
    form.setLatestCompletionMonth(maxFutureDate.getMonthValue());
    form.setLatestCompletionYear(maxFutureDate.getYear());

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(
            PwaApplicationType.OPTIONS_VARIATION,
            PwaResourceType.PETROLEUM,
            ValidationType.FULL,
            Set.of(ProjectInformationQuestion.PROPOSED_START_DATE,
                ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
                ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));

    assertThat(errorsMap).isEmpty();

  }

  @Test
  void validate_EarliestAndLatestCompletionSwap() {
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
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE, ProjectInformationQuestion.LATEST_COMPLETION_DATE), false));
    assertThat(errorsMap).containsValues(
        Set.of("latestCompletionDay.beforeDate"),
        Set.of("latestCompletionMonth.beforeDate"),
        Set.of("latestCompletionYear.beforeDate")
    );
  }


  @Test
  void validate_licenceTransferPlanned_noDatesProvided() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL,
            Set.of(
                ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED,
                ProjectInformationQuestion.LICENCE_TRANSFER_DATE,
                ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE,
                ProjectInformationQuestion.LICENCE_TRANSFER_REFERENCE), false));

    assertThat(errorsMap).contains(
        entry("pearsApplicationSelector", Set.of("pearsApplicationSelector.required")),

        entry("commercialAgreementDay", Set.of("commercialAgreementDay.required")),
        entry("commercialAgreementMonth", Set.of("commercialAgreementMonth.required")),
        entry("commercialAgreementYear", Set.of("commercialAgreementYear.required")),

        entry("licenceTransferDay", Set.of("licenceTransferDay.required")),
        entry("licenceTransferMonth", Set.of("licenceTransferMonth.required")),
        entry("licenceTransferYear", Set.of("licenceTransferYear.required"))
    );

  }

  @Test
  void validate_licenceTransferPlanned_validCommercialAgreementDate() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setCommercialAgreementDay(1);
    form.setCommercialAgreementMonth(2);
    form.setCommercialAgreementYear(2020);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM,
            ValidationType.FULL, Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE), false));

    assertThat(errorsMap).doesNotContainKeys(
        "commercialAgreementDay",
        "commercialAgreementMonth",
        "commercialAgreementYear"
    );

  }

  @Test
  void validate_licenceTransferPlanned_invalidCommercialAgreementDate() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setCommercialAgreementDay(100);
    form.setCommercialAgreementMonth(100);
    form.setCommercialAgreementYear(2020);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM,
            ValidationType.FULL, Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE), false));

    assertThat(errorsMap).contains(
        entry("commercialAgreementDay", Set.of("commercialAgreementDay.invalid")),
        entry("commercialAgreementMonth", Set.of("commercialAgreementMonth.invalid")),
        entry("commercialAgreementYear", Set.of("commercialAgreementYear.invalid"))
    );

  }

  @Test
  void validate_licenceTransferPlanned_validLicenceTransferDate() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setLicenceTransferDay(1);
    form.setLicenceTransferMonth(2);
    form.setLicenceTransferYear(2020);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.LICENCE_TRANSFER_DATE), false));

    assertThat(errorsMap).doesNotContainKeys(
        "licenceTransferDay",
        "licenceTransferMonth",
        "licenceTransferYear"
    );

  }

  @Test
  void validate_licenceTransferPlanned_invalidLicenceTransferDate() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setLicenceTransferDay(100);
    form.setLicenceTransferMonth(100);
    form.setLicenceTransferYear(2020);
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.LICENCE_TRANSFER_DATE), false));

    assertThat(errorsMap).contains(
        entry("licenceTransferDay", Set.of("licenceTransferDay.invalid")),
        entry("licenceTransferMonth", Set.of("licenceTransferMonth.invalid")),
        entry("licenceTransferYear", Set.of("licenceTransferYear.invalid"))
    );

  }

  @Test
  void validate_licenceTransferPlanned_validTransferReference() {

    when(pearsLicenceTransactionService.getApplicationsByIds(any())).thenReturn(List.of(new PearsLicenceTransaction()));
    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setPearsApplicationList(new String[]{"5555"});
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.LICENCE_TRANSFER_REFERENCE), false));

    assertThat(errorsMap).doesNotContainKey("pearsApplicationSelector");
  }

  @Test
  void validate_licenceTransferPlanned_invalidTransferReference() {

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setPearsApplicationList(new String[]{"5555"});
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL,
            Set.of(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED, ProjectInformationQuestion.LICENCE_TRANSFER_REFERENCE), false));

    assertThat(errorsMap).contains(entry("pearsApplicationSelector", Set.of("pearsApplicationSelector.invalid")));
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
  void validate_permanentDepositType_noValidationRequired() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(), false));
    assertThat(errorsMap).doesNotContainKey("permanentDepositsMadeType");
  }

  @Test
  void validate_permanentDepositType_Null() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("permanentDepositsMadeType", Set.of("permanentDepositsMadeType.notSelected"))
    );
  }

  @Test
  void validate_permanentDepositType_LaterApp_noDate() {
    var form = new ProjectInformationForm();
    form.setPermanentDepositsMadeType(PermanentDepositMade.LATER_APP);
    form.setFutureSubmissionDate(new TwoFieldDateInput());
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("futureSubmissionDate.month", Set.of("month.required")),
        entry("futureSubmissionDate.year", Set.of("year.required"))
    );
  }

  @Test
  void validate_permanentDepositType_LaterApp_pastDate() {
    var form = new ProjectInformationForm();
    form.setPermanentDepositsMadeType(PermanentDepositMade.LATER_APP);
    form.setFutureSubmissionDate(new TwoFieldDateInput(2020, 2));
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("futureSubmissionDate.month", Set.of("month.afterDate")),
        entry("futureSubmissionDate.year", Set.of("year.afterDate"))
    );
  }

  @Test
  void validate_temporaryDeposit_noDescription() {
    var form = new ProjectInformationForm();
    form.setTemporaryDepositsMade(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("temporaryDepDescription", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("temporaryDepDescription")))
    );
  }

  @Test
  void validate_partial_temporaryDepositDescriptionOverMaxLength() {
    var form = new ProjectInformationForm();
    form.setTemporaryDepositsMade(true);
    form.setTemporaryDepDescription(ValidatorTestUtils.overMaxDefaultCharLength());
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.PARTIAL, Set.of(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("temporaryDepDescription", Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("temporaryDepDescription")))
    );
  }


  @Test
  void validate_temporaryDeposit_Null() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE), false));
    assertThat(errorsMap).contains(
        entry("temporaryDepositsMade", Set.of(FieldValidationErrorCodes.REQUIRED.errorCode("temporaryDepositsMade")))
    );
  }


  @Test
  void validate_noFdpQuestionRequired() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(), false));
    assertThat(errorsMap).doesNotContain(
        entry("fdpOptionSelected", Set.of("fdpOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("fdpConfirmationFlag", Set.of("fdpConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("fdpNotSelectedReason", Set.of("fdpNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_fdpQuestionRequired_valid() {
    var form = new ProjectInformationForm();
    form.setFdpOptionSelected(true);
    form.setFdpConfirmationFlag(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN), true));
    assertThat(errorsMap).doesNotContain(
        entry("fdpOptionSelected", Set.of("fdpOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("fdpConfirmationFlag", Set.of("fdpConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("fdpNotSelectedReason", Set.of("fdpNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_fdpQuestionRequired_noFdpOptionSelected() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN), true));
    assertThat(errorsMap).contains(
        entry("fdpOptionSelected", Set.of("fdpOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_fdpQuestionRequired_fdpOptionSelected_fdpConfirmationFlagNotChecked() {
    var form = new ProjectInformationForm();
    form.setFdpOptionSelected(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN), true));
    assertThat(errorsMap).contains(
        entry("fdpConfirmationFlag", Set.of("fdpConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_fdpQuestionRequired_fdpOptionSelectedIsNo_fdpNotSelectedReasonEmpty() {
    var form = new ProjectInformationForm();
    form.setFdpOptionSelected(false);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN), true));
    assertThat(errorsMap).contains(
        entry("fdpNotSelectedReason", Set.of("fdpNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_partial_fdpNotSelectedReasonOverMaxLength() {
    var form = new ProjectInformationForm();
    form.setFdpOptionSelected(false);
    form.setFdpNotSelectedReason(ValidatorTestUtils.overMaxDefaultCharLength());
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.PARTIAL, Set.of(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN), true));
    assertThat(errorsMap).contains(
        entry("fdpNotSelectedReason", Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode("fdpNotSelectedReason")))
    );
  }

  @Test
  void validate_oneProjectLayoutDiagramFile() {
    var form = new ProjectInformationForm();

    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM), false));
    assertThat(errorsMap).doesNotContainKeys("uploadedFiles", "uploadedFiles[0].uploadedFileDescription");
  }

  @Test
  void validate_projectLayoutDiagramFileNotUploaded() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM), false));
    assertThat(errorsMap).contains(
        entry("uploadedFiles", Set.of(FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE))
    );
  }

  @Test
  void validate_tooManyProjectLayoutDiagramFiles() {
    var form = new ProjectInformationForm();

    form.setUploadedFiles(List.of(
        FileManagementValidatorTestUtils.createUploadedFileForm(),
        FileManagementValidatorTestUtils.createUploadedFileForm()
    ));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM), false));
    assertThat(errorsMap).containsKeys("uploadedFiles");
  }

  @Test
  void validate_projectLayoutDiagramFile_noDescription() {
    var form = new ProjectInformationForm();

    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM), false));
    assertThat(errorsMap).contains(
        entry("uploadedFiles[0].uploadedFileDescription",
            Set.of("uploadedFiles[0].uploadedFileDescription" + FieldValidationErrorCodes.REQUIRED.getCode())));
  }

  @Test
  void validate_partialValidation_noFullValidationErrorsPresent() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.PARTIAL, EnumSet.allOf(ProjectInformationQuestion.class), false));
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_validationNotRequired_whenQuestionNotProvided() {
    var form = new ProjectInformationForm();
    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form,
        new ProjectInformationFormValidationHints(PwaApplicationType.INITIAL, PwaResourceType.PETROLEUM, ValidationType.FULL, Set.of(), false));
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_noCspQuestionRequired() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.CCUS, ValidationType.FULL, Set.of(), false));
    assertThat(errorsMap).doesNotContain(
        entry("cspOptionSelected", Set.of("cspOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("cspConfirmationFlag", Set.of("cspConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("cspNotSelectedReason", Set.of("cspNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_cspQuestionRequired_valid() {
    var form = new ProjectInformationForm();
    form.setCspOptionSelected(true);
    form.setCspConfirmationFlag(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.CCUS, ValidationType.FULL, Set.of(ProjectInformationQuestion.CARBON_STORAGE_PERMIT), true));
    assertThat(errorsMap).doesNotContain(
        entry("cspOptionSelected", Set.of("cspOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("cspConfirmationFlag", Set.of("cspConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode())),
        entry("cspNotSelectedReason", Set.of("cspNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_cspQuestionRequired_noCspOptionSelected() {
    var form = new ProjectInformationForm();
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.CCUS, ValidationType.FULL, Set.of(ProjectInformationQuestion.CARBON_STORAGE_PERMIT), true));
    assertThat(errorsMap).contains(
        entry("cspOptionSelected", Set.of("cspOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_cspQuestionRequired_cspOptionSelected_cspConfirmationFlagNotChecked() {
    var form = new ProjectInformationForm();
    form.setCspOptionSelected(true);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.CCUS, ValidationType.FULL, Set.of(ProjectInformationQuestion.CARBON_STORAGE_PERMIT), true));
    assertThat(errorsMap).contains(
        entry("cspConfirmationFlag", Set.of("cspConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void validate_cspQuestionRequired_cspOptionSelectedIsNo_cspNotSelectedReasonEmpty() {
    var form = new ProjectInformationForm();
    form.setCspOptionSelected(false);
    Map<String, Set<String>> errorsMap = getErrorMap(form, new ProjectInformationFormValidationHints(
        PwaApplicationType.INITIAL, PwaResourceType.CCUS, ValidationType.FULL, Set.of(ProjectInformationQuestion.CARBON_STORAGE_PERMIT), true));
    assertThat(errorsMap).contains(
        entry("cspNotSelectedReason", Set.of("cspNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

}
