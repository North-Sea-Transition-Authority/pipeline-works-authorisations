package uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory;


import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ConfirmSatisfactoryApplicationFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ConfirmSatisfactoryApplicationForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    var form = (ConfirmSatisfactoryApplicationForm) target;

    ValidatorUtils.validateDefaultStringLength(
        errors,
        "reason",
        form::getReason,
        "Reason"
    );

  }

}
