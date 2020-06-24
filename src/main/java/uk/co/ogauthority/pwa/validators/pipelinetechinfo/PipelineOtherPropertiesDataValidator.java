package uk.co.ogauthority.pwa.validators.pipelinetechinfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesDataForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;


@Service
public class PipelineOtherPropertiesDataValidator implements SmartValidator {

  private final MinMaxInputValidator minMaxInputValidator;

  @Autowired
  public PipelineOtherPropertiesDataValidator(
      MinMaxInputValidator minMaxInputValidator) {
    this.minMaxInputValidator = minMaxInputValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineOtherPropertiesDataForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {

  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    var form = (PipelineOtherPropertiesDataForm) o;
    var property = (OtherPipelineProperty) validationHints[0];

    if (form.getPropertyAvailabilityOption() == null) {
      errors.rejectValue("propertyAvailabilityOption", "propertyAvailabilityOption" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select an availability option for " + property.getDisplayText());

    } else if (form.getPropertyAvailabilityOption().equals(PropertyAvailabilityOption.AVAILABLE)) {
      minMaxInputValidator.validate(form.getMinMaxInput(), errors, property, 2);
    }

  }



}
