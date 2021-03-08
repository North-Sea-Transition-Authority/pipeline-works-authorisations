package uk.co.ogauthority.pwa.validators;

import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.time.DateTimeException;
import java.time.LocalDate;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.pwaapplications.shared.EnvDecomQuestion;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class EnvironmentalDecommissioningValidator implements SmartValidator {

  private final PadEnvironmentalDecommissioningService environmentalDecommissioningService;

  @Autowired
  public EnvironmentalDecommissioningValidator(@Lazy PadEnvironmentalDecommissioningService environmentalDecommissioningService) {
    this.environmentalDecommissioningService = environmentalDecommissioningService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(EnvironmentalDecommissioningForm.class);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    EnvironmentalDecommissioningForm form = (EnvironmentalDecommissioningForm) target;
    var appDetail = (PwaApplicationDetail) validationHints[0];
    var validationType = (ValidationType) validationHints[1];

    var availableQuestions = environmentalDecommissioningService.getAvailableQuestions(appDetail);

    if (availableQuestions.contains(EnvDecomQuestion.TRANS_BOUNDARY) && validationType == ValidationType.FULL) {
      ValidationUtils.rejectIfEmpty(errors, "transboundaryEffect", REQUIRED.errorCode("transboundaryEffect"),
          "Select yes if the development has significant trans-boundary effects");
    }

    if (availableQuestions.contains(EnvDecomQuestion.BEIS_EMT_PERMITS)) {

      if (validationType == ValidationType.FULL) {

        ValidationUtils.rejectIfEmpty(
            errors,
            "emtHasSubmittedPermits",
            REQUIRED.errorCode("emtHasSubmittedPermits"),
            "Select yes if any relevant environmental permits have been submitted to BEIS"
        );

        ValidationUtils.rejectIfEmpty(
            errors,
            "emtHasOutstandingPermits",
            REQUIRED.errorCode("emtHasOutstandingPermits"),
            "Select yes if you have any relevant permits that haven't been submitted to BEIS"
        );

        if (BooleanUtils.isFalse(form.getEmtHasSubmittedPermits()) && BooleanUtils.isFalse(form.getEmtHasOutstandingPermits())) {
          errors.rejectValue("emtHasSubmittedPermits", "emtHasSubmittedPermits" + FieldValidationErrorCodes.INVALID.getCode(),
              "Select 'Yes' to one or both of the BEIS EMT permit questions");
          errors.rejectValue("emtHasOutstandingPermits", "emtHasOutstandingPermits" + FieldValidationErrorCodes.INVALID.getCode(),
              "Select 'Yes' to one or both of the BEIS EMT permit questions");
        }

      }

      ValidatorUtils.validateDefaultStringLength(errors, "permitsSubmitted", form::getPermitsSubmitted, "Permits submitted to BEIS");
      ValidatorUtils.validateDefaultStringLength(errors, "permitsPendingSubmission", form::getPermitsPendingSubmission,
          "Permits pending BEIS submission");

      validateBeisEmtPermits(errors, form, validationType);

    }

    if (availableQuestions.contains(EnvDecomQuestion.ACKNOWLEDGEMENTS) && validationType == ValidationType.FULL) {

      if (form.getEnvironmentalConditions() == null
          || form.getEnvironmentalConditions().size() < EnvironmentalCondition.values().length) {
        errors.rejectValue("environmentalConditions", "environmentalConditions.requiresAll",
            "Confirm your agreement to all environmental conditions");
      }

    }

    if (availableQuestions.contains(EnvDecomQuestion.DECOMMISSIONING) && validationType == ValidationType.FULL) {
      if (form.getDecommissioningConditions() == null
          || form.getDecommissioningConditions().size() < DecommissioningCondition.values().length) {
        errors.rejectValue("decommissioningConditions", "decommissioningConditions.requiresAll",
            "Confirm your agreement to all decommissioning conditions");
      }
    }

  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Use method with validation hints");
  }

  private void validateBeisEmtPermits(Errors errors, EnvironmentalDecommissioningForm form, ValidationType validationType) {

    if (BooleanUtils.isTrue(form.getEmtHasSubmittedPermits())) {
      if (StringUtils.isBlank(form.getPermitsSubmitted()) && validationType == ValidationType.FULL) {
        errors.rejectValue("permitsSubmitted", "permitsSubmitted.required", "Enter a list of the submitted permits");
      }
    }

    if (BooleanUtils.isTrue(form.getEmtHasOutstandingPermits())) {

      if (StringUtils.isBlank(form.getPermitsPendingSubmission()) && validationType == ValidationType.FULL) {
        errors.rejectValue("permitsPendingSubmission", "permitsPendingSubmission.required",
            "Enter a list of the permits you will submit at a later date");
      }

      if (validationType == ValidationType.FULL) {
        ValidatorUtils.validateDate(
            "emtSubmission", "permit submission",
            form.getEmtSubmissionDay(), form.getEmtSubmissionMonth(), form.getEmtSubmissionYear(), errors);

      } else {
        ValidatorUtils.validateDateWhenPresent(
            "emtSubmission", "permit submission",
            form.getEmtSubmissionDay(), form.getEmtSubmissionMonth(), form.getEmtSubmissionYear(), errors);
      }
    }

  }

}
