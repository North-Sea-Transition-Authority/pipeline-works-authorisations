package uk.co.ogauthority.pwa.validators.pipelinetechinfo;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.FluidCompositionOption;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionDataForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;


@Service
public class FluidCompositionDataValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return FluidCompositionDataForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

  }


  @Override
  public void validate(Object o, Errors errors, Object... validationHints) {
    var form = (FluidCompositionDataForm) o;
    var chemical = (Chemical) validationHints[0];

    if (form.getFluidCompositionOption() == null) {
      errors.rejectValue("fluidCompositionOption", "fluidCompositionOption" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select a fluid composition option for " + chemical.getDisplayText());

    } else if (form.getFluidCompositionOption().equals(FluidCompositionOption.HIGHER_AMOUNT)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "moleValue", "moleValue" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "You must enter a mole percentage for " + chemical.getDisplayText());

      if (form.getMoleValue() != null && form.getMoleValue().remainder(BigDecimal.ONE).precision() > 2) {
        errors.rejectValue("moleValue", "moleValue" + FieldValidationErrorCodes.INVALID.getCode(),
            "Mole percentage should not have more than 2dp for " + chemical.getDisplayText());

      } else if (form.getMoleValue() != null
          && (form.getMoleValue().compareTo(BigDecimal.valueOf(0.01)) == -1
          || form.getMoleValue().compareTo(BigDecimal.valueOf(100)) == 1)) {
        errors.rejectValue("moleValue", "moleValue" + FieldValidationErrorCodes.INVALID.getCode(),
            "Enter a mole percentage between 0.01 and 100 for " + chemical.getDisplayText());
      }

    }
  }

}

