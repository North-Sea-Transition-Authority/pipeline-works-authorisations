package uk.co.ogauthority.pwa.validators;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsSafetyZoneForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class LocationDetailsSafetyZoneValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(LocationDetailsSafetyZoneForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (LocationDetailsSafetyZoneForm) target;

    if (form.getWithinSafetyZone() == null) {
      errors.rejectValue("withinSafetyZone",
          "withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter information on work carried out within 500m of a safety zone");

    } else {
      switch (form.getWithinSafetyZone()) {
        case YES:
          if (form.getFacilitiesIfYes().size() == 0) {
            errors.rejectValue("facilitiesIfYes",
                "facilitiesIfYes" + FieldValidationErrorCodes.REQUIRED.getCode(),
                "Select all structures within 500m");
          }
          break;
        case PARTIALLY:
          if (form.getFacilitiesIfPartially().size() == 0) {
            errors.rejectValue("facilitiesIfPartially",
                "facilitiesIfPartially" + FieldValidationErrorCodes.REQUIRED.getCode(),
                "Select all structures within 500m");
          }
          break;
        default:
          break;
      }
    }
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    validate(target, errors);
  }








}
