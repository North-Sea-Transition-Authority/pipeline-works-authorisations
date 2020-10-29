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
    var requiredQuestions = projectInfoValidationHints.getRequiredQuestions();


    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_NAME)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectName",
          "projectName" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the project name");
      ValidatorUtils.validateDefaultStringLength(
          errors, "projectName", form::getProjectName, "Project name must be 4000 characters or fewer");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_OVERVIEW)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectOverview",
          "projectOverview" + FieldValidationErrorCodes.REQUIRED.getCode(), "Enter the project overview");
      ValidatorUtils.validateDefaultStringLength(
          errors, "projectOverview", form::getProjectOverview, "Project overview must be 4000 characters or fewer");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "methodOfPipelineDeployment",
          "methodOfPipelineDeployment" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter the pipeline installation method");
      ValidatorUtils.validateDefaultStringLength(
          errors, "methodOfPipelineDeployment", form::getMethodOfPipelineDeployment,
          "Pipeline installation method must be 4000 characters or fewer");
    }


    if (projectInfoValidationHints.getValidationType().equals(ValidationType.FULL)) {
      if (requiredQuestions.contains(ProjectInformationQuestion.PROPOSED_START_DATE)) {
        ValidatorUtils.validateDateIsPresentOrFuture(
            "proposedStart", "proposed start of works",
            form.getProposedStartDay(), form.getProposedStartMonth(), form.getProposedStartYear(), errors);
      }

      if (requiredQuestions.contains(ProjectInformationQuestion.MOBILISATION_DATE)) {
        ValidatorUtils.validateDateIsPresentOrFuture(
            "mobilisation", "mobilisation",
            form.getMobilisationDay(), form.getMobilisationMonth(), form.getMobilisationYear(), errors);
      }

      boolean earliestCompletionDateValid = false;
      if (requiredQuestions.contains(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE)) {
        earliestCompletionDateValid = ValidatorUtils.validateDateIsPresentOrFuture(
            "earliestCompletion", "earliest completion",
            form.getEarliestCompletionDay(), form.getEarliestCompletionMonth(), form.getEarliestCompletionYear(),
            errors);
      }

      boolean latestCompletionDateValid = false;
      if (requiredQuestions.contains(ProjectInformationQuestion.LATEST_COMPLETION_DATE)) {
        latestCompletionDateValid = ValidatorUtils.validateDateIsPresentOrFuture(
            "latestCompletion", "latest completion",
            form.getLatestCompletionDay(), form.getLatestCompletionMonth(), form.getLatestCompletionYear(), errors);
      }


      //if licence transfer and commercial agreement date required, ensure valid, else ignore.
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


      if (earliestCompletionDateValid && latestCompletionDateValid) {
        var earliestCompletion = LocalDate.of(form.getEarliestCompletionYear(), form.getEarliestCompletionMonth(),
            form.getEarliestCompletionDay());
        var latestCompletion = LocalDate.of(form.getLatestCompletionYear(), form.getLatestCompletionMonth(),
            form.getLatestCompletionDay());
        if (latestCompletion.isBefore(earliestCompletion)) {
          errors.rejectValue("latestCompletionDay", "latestCompletionDay.beforeStart",
              "Latest completion must not be before earliest completion");
          errors.rejectValue("latestCompletionMonth", "latestCompletionMonth.beforeStart", "");
          errors.rejectValue("latestCompletionYear", "latestCompletionYear.beforeStart", "");
        }
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
              "You must confirm the proposed works outlined in this application are consistent with the field development plan");
        } else if (!form.getFdpOptionSelected()) {
          ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fdpNotSelectedReason",
              "fdpNotSelectedReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
              "You must enter a reason for not having an FDP for the fields");
        }

      }

      if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM)
          && ListUtils.emptyIfNull(form.getUploadedFileWithDescriptionForms()).size() > 1) {
        errors.rejectValue("uploadedFileWithDescriptionForms",
            "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode(),
            "You must upload a maximum of one file");
      }

    }
  }


}
