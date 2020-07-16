package uk.co.ogauthority.pwa.validators.partnerletters;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;

@Service
public class PartnerLettersValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return PartnerLettersForm.class.equals(clazz);
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var partnerLettersForm = (PartnerLettersForm) target;

  }


  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }
}
