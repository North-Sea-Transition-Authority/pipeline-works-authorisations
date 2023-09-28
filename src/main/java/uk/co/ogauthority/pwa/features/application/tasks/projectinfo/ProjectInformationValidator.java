package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.MaxCompletionPeriod;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplicationService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

@Service
public class ProjectInformationValidator implements SmartValidator {


  private final TwoFieldDateInputValidator twoFieldDateInputValidator;

  private final PearsLicenceApplicationService pearsLicenceApplicationService;

  @Autowired
  public ProjectInformationValidator(TwoFieldDateInputValidator twoFieldDateInputValidator,
                                     PearsLicenceApplicationService pearsLicenceApplicationService) {
    this.twoFieldDateInputValidator = twoFieldDateInputValidator;
    this.pearsLicenceApplicationService = pearsLicenceApplicationService;
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
    var validationType = projectInfoValidationHints.getValidationType();

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_NAME)) {
      ValidatorUtils.validateMaxStringLength(
          errors, "projectName", form::getProjectName, "Project name", 150);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROPOSED_START_DATE) && validationType != ValidationType.FULL) {
      ValidatorUtils.validateDateWhenPresent(
          "proposedStart", "Proposed start of works",
          form.getProposedStartDay(), form.getProposedStartMonth(), form.getProposedStartYear(), errors);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_OVERVIEW)) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "projectOverview", form::getProjectOverview, "Project overview");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT)
        && form.getMethodOfPipelineDeployment() != null) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "methodOfPipelineDeployment", form::getMethodOfPipelineDeployment,
          "Pipeline installation method");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.MOBILISATION_DATE) && validationType != ValidationType.FULL) {
      ValidatorUtils.validateDateWhenPresent(
          "mobilisation", "Mobilisation",
          form.getMobilisationDay(), form.getMobilisationMonth(), form.getMobilisationYear(), errors);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE) && validationType != ValidationType.FULL) {
      ValidatorUtils.validateDateWhenPresent(
          "earliestCompletion", "Earliest completion",
          form.getEarliestCompletionDay(), form.getEarliestCompletionMonth(), form.getEarliestCompletionYear(),
          errors);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LATEST_COMPLETION_DATE) && validationType != ValidationType.FULL) {
      ValidatorUtils.validateDateWhenPresent(
          "latestCompletion", "Latest completion",
          form.getLatestCompletionDay(), form.getLatestCompletionMonth(), form.getLatestCompletionYear(), errors);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED) && validationType != ValidationType.FULL) {
      if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_DATE)
          && BooleanUtils.isTrue(form.getLicenceTransferPlanned())) {
        ValidatorUtils.validateDateWhenPresent(
            "licenceTransfer", "Licence transfer",
            form.getLicenceTransferDay(), form.getLicenceTransferMonth(), form.getLicenceTransferYear(), errors
        );
      }

      if (requiredQuestions.contains(ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE)
          && BooleanUtils.isTrue(form.getLicenceTransferPlanned())) {
        ValidatorUtils.validateDateWhenPresent(
            "commercialAgreement", "Commercial agreement",
            form.getCommercialAgreementDay(), form.getCommercialAgreementMonth(), form.getCommercialAgreementYear(), errors
        );
      }
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE)) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "temporaryDepDescription", form::getTemporaryDepDescription,
          "Temporary deposits description");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE)
        && PermanentDepositMade.LATER_APP.equals(form.getPermanentDepositsMadeType()) && validationType != ValidationType.FULL) {
      List<Object> toDateHints = new ArrayList<>();
      toDateHints.add(new FormInputLabel("deposit submission"));
      ValidatorUtils.invokeNestedValidator(
          errors,
          twoFieldDateInputValidator,
          "futureSubmissionDate",
          form.getFutureSubmissionDate(),
          toDateHints.toArray());
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN)
        && projectInfoValidationHints.isFdpQuestionRequired()) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "fdpNotSelectedReason", form::getFdpNotSelectedReason,
          "Description for not having an FDP");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM)
        && validationType.equals(ValidationType.PARTIAL)) {
      FileUploadUtils.validateFilesDescriptionLength(form, errors);
    }
  }

  private void validateFull(ProjectInformationForm form,  Errors errors,
        ProjectInformationFormValidationHints projectInfoValidationHints) {

    var requiredQuestions = projectInfoValidationHints.getRequiredQuestions();
    var applicationType = projectInfoValidationHints.getPwaApplicationType();

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_NAME)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectName",
          "projectName" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the project name");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_OVERVIEW)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectOverview",
          "projectOverview" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the project overview");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT)
        && !ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT.isOptionalForType(applicationType)) {

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "methodOfPipelineDeployment",
          "methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter the pipeline installation method");
    }

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

        var maxCompletionPeriod = MaxCompletionPeriod.valueOf(applicationType.name());
        var proposedStartDate = LocalDate.of(form.getProposedStartYear(), form.getProposedStartMonth(),
            form.getProposedStartDay());

        if (!maxCompletionPeriod.isExtendable()) {
          ValidatorUtils.validateDateIsOnOrBeforeComparisonDate(
              "latestCompletion",
              "Latest completion date",
              form.getLatestCompletionDay(),
              form.getLatestCompletionMonth(),
              form.getLatestCompletionYear(),
              proposedStartDate
                  .plusMonths(maxCompletionPeriod.getMaxMonthsCompletion())
                  .minusDays(1),
              String.format("more than %s months after proposed start date", maxCompletionPeriod.getMaxMonthsCompletion()),
              errors
          );
        }
      }

    }


    if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED)) {
      var transferPlanned = BooleanUtils.isTrue(form.getLicenceTransferPlanned());

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "licenceTransferPlanned",
          "licenceTransferPlanned" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select yes if a licence transfer is planned");

      if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_DATE) && transferPlanned) {
        ValidatorUtils.validateDate(
            "licenceTransfer", "licence transfer",
            form.getLicenceTransferDay(),
            form.getLicenceTransferMonth(),
            form.getLicenceTransferYear(),
            errors
        );
      }

      if (requiredQuestions.contains(ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE) && transferPlanned) {
        ValidatorUtils.validateDate(
            "commercialAgreement", "commercial agreement",
            form.getCommercialAgreementDay(),
            form.getCommercialAgreementMonth(),
            form.getCommercialAgreementYear(),
            errors
        );
      }

      if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_REFERENCE) && transferPlanned) {
        if (Objects.isNull(form.getPearsApplicationList())
            || form.getPearsApplicationList().length == 0
            || Arrays.stream(form.getPearsApplicationList()).anyMatch(Objects::isNull)) {
          errors.rejectValue("pearsApplicationSelector",
              "pearsApplicationSelector" + FieldValidationErrorCodes.REQUIRED.getCode(),
              "Select at least one application relating to the licence transfer");
        } else {
          var applicationIds = Arrays.stream(form.getPearsApplicationList())
              .map(Integer::valueOf)
              .collect(Collectors.toList());
          if (pearsLicenceApplicationService.getApplicationsByIds(applicationIds).size() != applicationIds.size()) {
            errors.rejectValue("pearsApplicationSelector",
                "pearsApplicationSelector" + FieldValidationErrorCodes.INVALID.getCode(),
                "Licence transfer application reference is invalid");
          }
        }
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
      } else if (form.getPermanentDepositsMadeType().equals(PermanentDepositMade.LATER_APP)) {
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

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "temporaryDepositsMade",
          "temporaryDepositsMade" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select yes if temporary deposits are being made");

      if (BooleanUtils.isTrue(form.getTemporaryDepositsMade())) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "temporaryDepDescription",
            "temporaryDepDescription" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter why temporary deposits are being made");
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

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM)) {
      FileUploadUtils.validateFiles(form, errors, List.of(MandatoryUploadValidation.class), "Upload a project layout diagram");
      FileUploadUtils.validateMaxFileLimit(form, errors, 1, "Upload a maximum of one file");
    }

  }


}
