package uk.co.ogauthority.pwa.validators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.ProjectInformationQuestion;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositRadioOption;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@Service
public class ProjectInformationValidator implements SmartValidator {


  private final TwoFieldDateInputValidator twoFieldDateInputValidator;

  public ProjectInformationValidator(TwoFieldDateInputValidator twoFieldDateInputValidator) {
    this.twoFieldDateInputValidator = twoFieldDateInputValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ProjectInformationForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {

  }

  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {

    var form = (ProjectInformationForm) o;
    var projectInfoValidationHints = (ProjectInformationFormValidationHints) validationHints[0];

    validatePartial(form, errors, projectInfoValidationHints);
    if (projectInfoValidationHints.getValidationType().equals(ValidationType.FULL)) {
      validateFull(form, errors, projectInfoValidationHints);
    }
  }

  private void validatePartial(ProjectInformationForm form,
                               Errors errors,
                               ProjectInformationFormValidationHints projectInfoValidationHints) {

    var requiredQuestions = projectInfoValidationHints.getRequiredQuestions();
    var applicationType = projectInfoValidationHints.getPwaApplicationType();

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_NAME)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectName",
          "projectName" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the project name");
      ValidatorUtils.validateDefaultStringLength(
          errors, "projectName", form::getProjectName, "Project name must be 4000 characters or fewer");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROPOSED_START_DATE)) {
      ValidatorUtils.validateYearWhenPresent(
          "proposedStart", "Proposed start of works",
          form.getProposedStartYear(), errors);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_OVERVIEW)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectOverview",
          "projectOverview" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the project overview");
      ValidatorUtils.validateDefaultStringLength(
          errors, "projectOverview", form::getProjectOverview, "Project overview must be 4000 characters or fewer");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT)) {

      if (!ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT.isOptionalForType(applicationType)) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "methodOfPipelineDeployment",
            "methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter the pipeline installation method");
      }

      if (form.getMethodOfPipelineDeployment() != null) {
        ValidatorUtils.validateDefaultStringLength(
            errors, "methodOfPipelineDeployment", form::getMethodOfPipelineDeployment,
            "Pipeline installation method must be 4000 characters or fewer");
      }
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.MOBILISATION_DATE)) {
      ValidatorUtils.validateYearWhenPresent(
          "mobilisation", "Mobilisation",
          form.getMobilisationYear(), errors);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE)) {
      ValidatorUtils.validateYearWhenPresent(
          "earliestCompletion", "Earliest completion",
          form.getEarliestCompletionYear(),
          errors);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LATEST_COMPLETION_DATE)) {
      ValidatorUtils.validateYearWhenPresent(
          "latestCompletion", "Latest completion",
          form.getLatestCompletionYear(), errors);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED)) {
      if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_DATE)
          && BooleanUtils.isTrue(form.getLicenceTransferPlanned())) {
        ValidatorUtils.validateYearWhenPresent(
            "licenceTransfer", "Licence transfer",
            form.getLicenceTransferYear(), errors
        );
      }

      if (requiredQuestions.contains(ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE)
          && BooleanUtils.isTrue(form.getLicenceTransferPlanned())) {
        ValidatorUtils.validateYearWhenPresent(
            "commercialAgreement", "Commercial agreement",
            form.getCommercialAgreementYear(), errors
        );
      }
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE)
        && PermanentDepositRadioOption.LATER_APP.equals(form.getPermanentDepositsMadeType())) {

      List<Object> toDateHints = new ArrayList<>();
      toDateHints.add(new FormInputLabel("deposit submission"));
      ValidatorUtils.invokeNestedValidator(
          errors,
          twoFieldDateInputValidator,
          "futureSubmissionDate",
          form.getFutureSubmissionDate(),
          toDateHints.toArray());
    }

  }

  private void validateFull(ProjectInformationForm form,  Errors errors,
        ProjectInformationFormValidationHints projectInfoValidationHints) {

    var requiredQuestions = projectInfoValidationHints.getRequiredQuestions();
    var applicationType = projectInfoValidationHints.getPwaApplicationType();

    var proposedStartDateValid = false;
    if (requiredQuestions.contains(ProjectInformationQuestion.PROPOSED_START_DATE)) {
      proposedStartDateValid = ValidatorUtils.validateDateIsPresentOrFuture(
          "proposedStart", "proposed start of works",
          form.getProposedStartDay(), form.getProposedStartMonth(), form.getProposedStartYear(), errors);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.MOBILISATION_DATE)) {

      var dateNotInPast = ValidatorUtils.validateDateIsPresentOrFuture(
          "mobilisation", "mobilisation",
          form.getMobilisationDay(), form.getMobilisationMonth(), form.getMobilisationYear(), errors);

      if (dateNotInPast && proposedStartDateValid) {

        var proposedStartDate = LocalDate.of(form.getProposedStartYear(), form.getProposedStartMonth(), form.getProposedStartDay());

        ValidatorUtils.validateDateIsOnOrBeforeComparisonDate(
            "mobilisation",
            "Mobilisation date",
            form.getMobilisationDay(),
            form.getMobilisationMonth(),
            form.getMobilisationYear(),
            proposedStartDate,
            "after proposed start date",
            errors
        );

      }
    }

    //if licence transfer and commercial agreement date required, ensure valid, else ignore.
    boolean earliestCompletionDateValid = false;
    if (requiredQuestions.contains(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE)) {

      earliestCompletionDateValid = ValidatorUtils.validateDateIsPresentOrFuture(
          "earliestCompletion", "earliest completion",
          form.getEarliestCompletionDay(), form.getEarliestCompletionMonth(), form.getEarliestCompletionYear(),
          errors);

      if (earliestCompletionDateValid && proposedStartDateValid) {

        var proposedStartDate = LocalDate.of(form.getProposedStartYear(), form.getProposedStartMonth(), form.getProposedStartDay());

        ValidatorUtils.validateDateIsOnOrAfterComparisonDate(
            "earliestCompletion",
            "Earliest completion date",
            form.getEarliestCompletionDay(),
            form.getEarliestCompletionMonth(),
            form.getEarliestCompletionYear(),
            proposedStartDate,
            "before proposed start date",
            errors
        );

      }
    }

    boolean latestCompletionDateValid;
    if (requiredQuestions.contains(ProjectInformationQuestion.LATEST_COMPLETION_DATE)) {

      String fieldPrefix = "latestCompletion";

      latestCompletionDateValid = ValidatorUtils.validateDateIsPresentOrFuture(
          fieldPrefix, "latest completion",
          form.getLatestCompletionDay(), form.getLatestCompletionMonth(), form.getLatestCompletionYear(), errors);

      // check that latest completion is after earliest completion first
      boolean latestCompletionAfterEarliestCompletion = false;
      if (latestCompletionDateValid && earliestCompletionDateValid) {

        var earliestCompletionDate = LocalDate.of(
            form.getEarliestCompletionYear(),
            form.getEarliestCompletionMonth(),
            form.getEarliestCompletionDay());

        latestCompletionAfterEarliestCompletion = ValidatorUtils.validateDateIsOnOrAfterComparisonDate(
            fieldPrefix,
            "Latest completion date",
            form.getLatestCompletionDay(),
            form.getLatestCompletionMonth(),
            form.getLatestCompletionYear(),
            earliestCompletionDate,
            "before earliest completion date",
            errors
        );

      }

      // check latest completion against proposed start if earliest completion isn't valid or latest completion is
      // after earliest completion. this is to avoid multiple error messages on this widget when both rules are invalid.
      if (latestCompletionDateValid && proposedStartDateValid
          && (!earliestCompletionDateValid || latestCompletionAfterEarliestCompletion)) {

        var validMonthsAfterProposedStart = applicationType.equals(PwaApplicationType.OPTIONS_VARIATION) ? 6L : 12L;
        var proposedStartDate = LocalDate.of(form.getProposedStartYear(), form.getProposedStartMonth(),
            form.getProposedStartDay());

        var maxValidDate = proposedStartDate.plusMonths(validMonthsAfterProposedStart);

        ValidatorUtils.validateDateIsOnOrBeforeComparisonDate(
            "latestCompletion",
            "Latest completion date",
            form.getLatestCompletionDay(),
            form.getLatestCompletionMonth(),
            form.getLatestCompletionYear(),
            maxValidDate,
            String.format("more than %s months after proposed start date", validMonthsAfterProposedStart),
            errors
        );

      }

    }


    if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED)) {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "licenceTransferPlanned",
          "licenceTransferPlanned" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select yes if a licence transfer is planned");

      if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_DATE)
          && BooleanUtils.isTrue(form.getLicenceTransferPlanned())) {
        ValidatorUtils.validateDate(
            "licenceTransfer", "licence transfer",
            form.getLicenceTransferDay(),
            form.getLicenceTransferMonth(),
            form.getLicenceTransferYear(),
            errors
        );
      }

      if (requiredQuestions.contains(ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE)
          && BooleanUtils.isTrue(form.getLicenceTransferPlanned())) {
        ValidatorUtils.validateDate(
            "commercialAgreement", "commercial agreement",
            form.getCommercialAgreementDay(),
            form.getCommercialAgreementMonth(),
            form.getCommercialAgreementYear(),
            errors
        );
      }
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.USING_CAMPAIGN_APPROACH)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "usingCampaignApproach",
          "usingCampaignApproach" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select yes if using a campaign approach");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE)) {
      if (form.getPermanentDepositsMadeType() == null) {
        errors.rejectValue("permanentDepositsMadeType", "permanentDepositsMadeType.notSelected",
            "Select yes if permanent deposits are being made");
      } else if (form.getPermanentDepositsMadeType().equals(PermanentDepositRadioOption.LATER_APP)) {
        List<Object> toDateHints = new ArrayList<>();
        toDateHints.add(new FormInputLabel("Submission date"));
        toDateHints.add(new OnOrAfterDateHint(LocalDate.now(), "current date"));
        ValidatorUtils.invokeNestedValidator(
            errors,
            twoFieldDateInputValidator,
            "futureSubmissionDate",
            form.getFutureSubmissionDate(),
            toDateHints.toArray());
      }
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE)) {
      if (form.getTemporaryDepositsMade() == null) {
        errors.rejectValue("temporaryDepositsMade", "temporaryDepositsMade.notSelected",
            "Select yes if temporary deposits are being made");

      } else if (form.getTemporaryDepositsMade() && form.getTemporaryDepDescription() == null) {
        errors.rejectValue("temporaryDepDescription", "temporaryDepDescription.empty",
            "Enter why temporary deposits are being made.");
      }
    }


    if (requiredQuestions.contains(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN)
        && projectInfoValidationHints.isFdpQuestionRequired()) {
      if (form.getFdpOptionSelected() == null) {
        errors.rejectValue("fdpOptionSelected", "fdpOptionSelected" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Select yes if you have an approved field development plan");
      } else if (form.getFdpOptionSelected() && !BooleanUtils.toBooleanDefaultIfNull(form.getFdpConfirmationFlag(),
          false)) {
        errors.rejectValue("fdpConfirmationFlag",
            "fdpConfirmationFlag" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Confirm the proposed works outlined in this application are consistent with the field development plan");
      } else if (!form.getFdpOptionSelected()) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fdpNotSelectedReason",
            "fdpNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter a reason for not having an FDP for the fields");
      }

    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM)
        && ListUtils.emptyIfNull(form.getUploadedFileWithDescriptionForms()).size() > 1) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode(),
          "Upload a maximum of one file");
    }
  }


}
