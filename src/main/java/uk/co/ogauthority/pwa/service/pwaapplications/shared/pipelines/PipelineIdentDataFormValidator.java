package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;

@Service
public class PipelineIdentDataFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineIdentDataForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new AssertionError(); /* required by the SmartValidator. Not actually used. */
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    // if being used as a sub-form, need to know the name of the sub-form to create the errors properly
    var fieldPrefix = validationHints[0] != null ? validationHints[0] + "." : "";

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "productsToBeConveyed", "productsToBeConveyed.required",
        "Enter the products to be conveyed");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "externalDiameter", "externalDiameter.required",
        "Enter the external diameter");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "internalDiameter", "internalDiameter.required",
        "Enter the internal diameter");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "wallThickness", "wallThickness.required",
        "Enter the wall thickness");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "maop", "maop.required",
        "Enter the MAOP");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "insulationCoatingType", "insulationCoatingType.required",
        "Enter the insulation / coating type");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldPrefix + "componentPartsDescription", "componentPartsDescription.required",
        "Enter a description of the component parts");

  }

}
