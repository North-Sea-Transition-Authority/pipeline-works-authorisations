package uk.co.ogauthority.pwa.validators.asbuilt;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.asbuilt.ChangeAsBuiltNotificationGroupDeadlineForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ChangeAsBuiltNotificationGroupDeadlineValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ChangeAsBuiltNotificationGroupDeadlineForm.class);
  }

  @Override
  public void validate(Object o, Errors errors) {
    var form = (ChangeAsBuiltNotificationGroupDeadlineForm) o;
    ValidatorUtils.validateDatePickerDateIsPresentOrFuture("newDeadlineDateTimestampStr",
        "deadline date", form.getNewDeadlineDateTimestampStr(), errors);
  }

}
