package uk.co.ogauthority.pwa.validators.pipelinetechinfo;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;


@Service
public class PadDesignOpConditionsValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(DesignOpConditionsForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (DesignOpConditionsForm) target;





  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
  }





}
