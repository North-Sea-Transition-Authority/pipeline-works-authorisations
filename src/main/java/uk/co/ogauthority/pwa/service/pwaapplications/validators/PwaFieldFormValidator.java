package uk.co.ogauthority.pwa.service.pwaapplications.validators;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;

@Service
public class PwaFieldFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return PwaFieldForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var fieldForm = (PwaFieldForm) target;
    if (BooleanUtils.isTrue(fieldForm.getLinkedToField()) && fieldForm.getFieldId() == null) {
      errors.rejectValue("fieldId", "NO_FIELD_ID", "Field must be selected");
    }
  }
}
