package uk.co.ogauthority.pwa.service.pwaapplications.initial.validators;

import java.time.DateTimeException;
import java.time.LocalDate;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
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
      if (form.getPermitsSubmitted() == null || form.getPermitsSubmitted().isBlank()) {
        errors.rejectValue("permitsSubmitted", "NO_CONTENT", "You must provide a list of the submitted permits");
      }
    }
    if (BooleanUtils.isTrue(form.getEmtHasOutstandingPermits())) {
      if (form.getPermitsPendingSubmission() == null || form.getPermitsPendingSubmission().isBlank()) {
        errors.rejectValue("permitsPendingSubmission", "NO_CONTENT",
            "You must provide a list of the permits you will submit at a later date");
      }
      try {
        LocalDate.of(
            form.getEmtSubmissionYear(),
            form.getEmtSubmissionMonth(),
            form.getEmtSubmissionDay()
        );
      } catch (NullPointerException npe) {
        errors.rejectValue("emtSubmissionDay", "NO_CONTENT", "You must enter all date fields");
        errors.rejectValue("emtSubmissionMonth", "NO_CONTENT", "You must enter all date fields");
        errors.rejectValue("emtSubmissionYear", "NO_CONTENT", "You must enter all date fields");
      } catch (DateTimeException dte) {
        errors.rejectValue("emtSubmissionDay", "INVALID_DATE", "The date entered is not valid");
        errors.rejectValue("emtSubmissionMonth", "INVALID_DATE", "The date entered is not valid");
        errors.rejectValue("emtSubmissionYear", "INVALID_DATE", "The date entered is not valid");
      }
    }
  }
}
