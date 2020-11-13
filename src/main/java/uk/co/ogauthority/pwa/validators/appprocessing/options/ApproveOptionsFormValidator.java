package uk.co.ogauthority.pwa.validators.appprocessing.options;


import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.appprocessing.options.ApproveOptionsForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ApproveOptionsFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ApproveOptionsForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (ApproveOptionsForm) target;

    ValidatorUtils.validateDateIsPresentOrFuture(
        "deadlineDate",
        "deadline",
        form.getDeadlineDateDay(),
        form.getDeadlineDateMonth(),
        form.getDeadlineDateYear(),
        errors
    );

  }
}
