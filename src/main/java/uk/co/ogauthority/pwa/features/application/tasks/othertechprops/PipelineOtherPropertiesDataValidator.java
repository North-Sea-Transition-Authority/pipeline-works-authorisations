package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.minmax.DecimalPlacesHint;
import uk.co.ogauthority.pwa.util.forminputs.minmax.IntegerHint;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;
import uk.co.ogauthority.pwa.util.forminputs.minmax.PositiveNumberHint;


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
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    var form = (PipelineOtherPropertiesDataForm) o;
    var formProperty = (OtherPipelineProperty) validationHints[0];
    var validationType = (ValidationType) validationHints[1];

    if (validationType.equals(ValidationType.FULL)) {
      if (form.getPropertyAvailabilityOption() == null) {
        errors.rejectValue("propertyAvailabilityOption", "propertyAvailabilityOption" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Select an availability option for " + formProperty.getDisplayText());

      }
    }
    if (form.getPropertyAvailabilityOption() != null
          && form.getPropertyAvailabilityOption().equals(PropertyAvailabilityOption.AVAILABLE)) {
      validateMinMaxInput(form.getMinMaxInput(), errors, formProperty, validationType);
    }
  }


  private void validateMinMaxInput(MinMaxInput minMaxInput,
                                   Errors errors,
                                   OtherPipelineProperty formProperty,
                                   ValidationType validationType) {
    List<Object> validationHints = new ArrayList<>();

    if (formProperty.equals(OtherPipelineProperty.WAX_CONTENT)) {
      validationHints = List.of(new PositiveNumberHint(), new DecimalPlacesHint(1));

    } else if (formProperty.equals(OtherPipelineProperty.WAX_APPEARANCE_TEMPERATURE)) {
      validationHints = List.of(new IntegerHint());

    } else if (formProperty.equals(OtherPipelineProperty.ACID_NUM)) {
      validationHints = List.of(new PositiveNumberHint(), new IntegerHint());

    } else if (formProperty.equals(OtherPipelineProperty.VISCOSITY)) {
      validationHints = List.of(new PositiveNumberHint(), new DecimalPlacesHint(1));
      
    } else if (formProperty.equals(OtherPipelineProperty.DENSITY_GRAVITY)) {
      validationHints = List.of(new PositiveNumberHint(), new IntegerHint());

    } else if (formProperty.equals(OtherPipelineProperty.SULPHUR_CONTENT)) {
      validationHints = List.of(new PositiveNumberHint(), new DecimalPlacesHint(2));

    } else if (formProperty.equals(OtherPipelineProperty.POUR_POINT)) {
      validationHints = List.of(new IntegerHint());

    } else if (formProperty.equals(OtherPipelineProperty.SOLID_CONTENT)) {
      validationHints = List.of(new PositiveNumberHint(), new DecimalPlacesHint(2));

    } else if (formProperty.equals(OtherPipelineProperty.MERCURY)) {
      validationHints = List.of(new PositiveNumberHint(), new IntegerHint());

    }

    ValidatorUtils.invokeNestedValidator(errors, minMaxInputValidator,
        "minMaxInput", minMaxInput, formProperty.getDisplayText(), List.of(), validationHints, validationType);

  }



}
