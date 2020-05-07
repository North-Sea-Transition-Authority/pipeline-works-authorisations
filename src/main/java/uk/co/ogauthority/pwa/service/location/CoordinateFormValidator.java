package uk.co.ogauthority.pwa.service.location;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.model.form.enums.ValueRequirement;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class CoordinateFormValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(CoordinateForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new AssertionError(); /* required by the SmartValidator. Not actually used. */
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (CoordinateForm) target;
    String fieldPrefix = validationHints[0] != null ? validationHints[0] + "." : "";
    var required = (ValueRequirement) validationHints[1];

    if (required.equals(ValueRequirement.OPTIONAL) && (
        form.getLatitudeDegrees() != null || form.getLatitudeMinutes() != null || form.getLatitudeSeconds() != null
        || form.getLongitudeDegrees() != null || form.getLongitudeMinutes() != null || form.getLongitudeSeconds() != null)
        || required.equals(ValueRequirement.MANDATORY)) {

      ValidatorUtils.validateLatitude(
          errors,
          Pair.of(fieldPrefix + "latitudeDegrees", form.getLatitudeDegrees()),
          Pair.of(fieldPrefix + "latitudeMinutes", form.getLatitudeMinutes()),
          Pair.of(fieldPrefix + "latitudeSeconds", form.getLatitudeSeconds()));

      ValidatorUtils.validateLongitude(
          errors,
          Pair.of(fieldPrefix + "longitudeDegrees", form.getLongitudeDegrees()),
          Pair.of(fieldPrefix + "longitudeMinutes", form.getLongitudeMinutes()),
          Pair.of(fieldPrefix + "longitudeSeconds", form.getLongitudeSeconds()),
          Pair.of(fieldPrefix + "longitudeDirection", form.getLongitudeDirection()));

    }

  }

}
