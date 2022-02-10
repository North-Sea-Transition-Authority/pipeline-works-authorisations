package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class LocationDetailsSafetyZoneValidator implements SmartValidator {


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(LocationDetailsSafetyZoneForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }


  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (LocationDetailsSafetyZoneForm) target;
    var validationType = (ValidationType) validationHints[0];

    if (validationType.equals(ValidationType.FULL)) {
      if (form.getFacilities().isEmpty()) {
        errors.rejectValue("facilities",
            "facilities" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Select all structures within 500m");
      }
    }
  }













}
