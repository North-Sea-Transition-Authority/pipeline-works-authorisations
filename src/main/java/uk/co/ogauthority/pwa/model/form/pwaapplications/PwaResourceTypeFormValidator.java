package uk.co.ogauthority.pwa.model.form.pwaapplications;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
public class PwaResourceTypeFormValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PwaResourceTypeForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (PwaResourceTypeForm) target;
    if (form.getResourceType() == null) {
      errors.rejectValue("resourceType", "resourceType.required", "Select a resource type");
    }
  }
}
