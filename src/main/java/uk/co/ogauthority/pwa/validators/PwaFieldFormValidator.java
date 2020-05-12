package uk.co.ogauthority.pwa.validators;

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
      errors.rejectValue("fieldId", "fieldId.required", "Field must be selected");
    } else if (BooleanUtils.isFalse(fieldForm.getLinkedToField())
            && (fieldForm.getNoLinkedFieldDescription() == null || fieldForm.getNoLinkedFieldDescription() == "")) {
      errors.rejectValue("noLinkedFieldDescription", "noLinkedFieldDescription.required", "Description must not be empty");
    }
  }
}
