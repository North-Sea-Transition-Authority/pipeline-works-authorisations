package uk.co.ogauthority.pwa.service.pwaapplications.validators;

import java.time.DateTimeException;
import java.time.LocalDate;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.EnvDecomForm;

@Service
public class PadEnvDecomValidator implements Validator {


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(EnvDecomForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    EnvDecomForm form = (EnvDecomForm) target;
    if (BooleanUtils.isTrue(form.getEmtHasSubmittedPermits())) {
      if (StringUtils.isBlank(form.getPermitsSubmitted())) {
        errors.rejectValue("permitsSubmitted", "permitsSubmitted.empty", "Enter a list of the submitted permits");
      }
    }
    if (BooleanUtils.isTrue(form.getEmtHasOutstandingPermits())) {
      if (StringUtils.isBlank(form.getPermitsPendingSubmission())) {
        errors.rejectValue("permitsPendingSubmission", "permitsPendingSubmission.empty",
            "Enter a list of the permits you will submit at a later date");
      }
      try {
        LocalDate.of(
            form.getEmtSubmissionYear(),
            form.getEmtSubmissionMonth(),
            form.getEmtSubmissionDay()
        );
      } catch (NullPointerException npe) {
        errors.rejectValue("emtSubmissionDay", "emtSubmissionDay.notParsable",
            "You must provide all submission date values");
        errors.rejectValue("emtSubmissionMonth", "emtSubmissionMonth.notParsable",
            "You must provide all submission date values");
        errors.rejectValue("emtSubmissionYear", "emtSubmissionYear.notParsable",
            "You must provide all submission date values");
      } catch (DateTimeException dte) {
        errors.rejectValue("emtSubmissionDay", "emtSubmissionDay.invalidDate",
            "You must provide a real date for submission");
        errors.rejectValue("emtSubmissionMonth", "emtSubmissionMonth.invalidDate",
            "You must provide a real date for submission");
        errors.rejectValue("emtSubmissionYear", "emtSubmissionYear.invalidDate",
            "You must provide a real date for submission");
      }
    }
    if (form.getEnvironmentalConditions() == null
        || form.getEnvironmentalConditions().size() < EnvironmentalCondition.values().length) {
      errors.rejectValue("environmentalConditions", "environmentalConditions.requiresAll",
          "You must agree to all environmental conditions");
    }
    if (form.getDecommissioningConditions() == null
        || form.getDecommissioningConditions().size() < DecommissioningCondition.values().length) {
      errors.rejectValue("decommissioningConditions", "decommissioningConditions.requiresAll",
          "You must agree to all decommissioning conditions");
    }
  }
}
