package uk.co.ogauthority.pwa.validators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositRadioOption;
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
    ValidatorUtils.validateDateIsPresentOrFuture(
        "proposedStart", "proposed start",
        form.getProposedStartDay(), form.getProposedStartMonth(), form.getProposedStartYear(), errors);
    ValidatorUtils.validateDateIsPresentOrFuture(
        "mobilisation", "mobilisation",
        form.getMobilisationDay(), form.getMobilisationMonth(), form.getMobilisationYear(), errors);
    var earliestCompletionDateValid = ValidatorUtils.validateDateIsPresentOrFuture(
        "earliestCompletion", "earliest completion",
        form.getEarliestCompletionDay(), form.getEarliestCompletionMonth(), form.getEarliestCompletionYear(), errors);
    var latestCompletionDateValid = ValidatorUtils.validateDateIsPresentOrFuture(
        "latestCompletion", "latest completion",
        form.getLatestCompletionDay(), form.getLatestCompletionMonth(), form.getLatestCompletionYear(), errors);

    //if licence transfer and commercial agreement date required, ensure valid, else ignore.
    if (BooleanUtils.isTrue(form.getLicenceTransferPlanned())) {
      var licenceTransferValid = ValidatorUtils.validateDate(
          "licenceTransfer", "licence transfer",
          form.getLicenceTransferDay(),
          form.getLicenceTransferMonth(),
          form.getLicenceTransferYear(),
          errors
      );

      var commercialAgreementValid = ValidatorUtils.validateDate(
          "commercialAgreement", "commercial agreement",
          form.getCommercialAgreementDay(),
          form.getCommercialAgreementMonth(),
          form.getCommercialAgreementYear(),
          errors
      );
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

    var projectInfoValidationHints = (ProjectInformationFormValidationHints) validationHints[0];
    if (projectInfoValidationHints.isAnyDepositQuestionRequired()) {
      if (projectInfoValidationHints.isPermanentDepositQuestionRequired()) {
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

      if (form.getTemporaryDepositsMade() == null) {
        errors.rejectValue("temporaryDepositsMade", "temporaryDepositsMade.notSelected",
                "Select yes if temporary deposits are being made");

      } else if (form.getTemporaryDepositsMade() && form.getTemporaryDepDescription() == null) {
        errors.rejectValue("temporaryDepDescription", "temporaryDepDescription.empty",
                "Enter why temporary deposits are being made.");
      }
    }
  }


}
