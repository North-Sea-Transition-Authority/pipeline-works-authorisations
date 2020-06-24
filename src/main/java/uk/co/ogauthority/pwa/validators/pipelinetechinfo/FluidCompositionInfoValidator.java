package uk.co.ogauthority.pwa.validators.pipelinetechinfo;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionInfoForm;


@Service
public class FluidCompositionInfoValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(FluidCompositionInfoForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (FluidCompositionInfoForm) target;




  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
  }





}
