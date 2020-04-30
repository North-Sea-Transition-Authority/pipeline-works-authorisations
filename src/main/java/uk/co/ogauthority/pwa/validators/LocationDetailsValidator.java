package uk.co.ogauthority.pwa.validators;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class LocationDetailsValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(LocationDetailsForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (LocationDetailsForm) target;
    if (form.getWithinSafetyZone() == null) {
      errors.rejectValue("withinSafetyZone", "withinSafetyZone.required",
          "You must provide information on work carried out within 500m of a safety zone");
    } else {
      switch (form.getWithinSafetyZone()) {
        case YES:
          if (form.getFacilitiesIfYes().size() == 0) {
            errors.rejectValue("facilitiesIfYes", "facilitiesIfYes.required",
                "You must provide all structures within 500m");
          }
          break;
        case PARTIALLY:
          if (form.getFacilitiesIfPartially().size() == 0) {
            errors.rejectValue("facilitiesIfPartially", "facilitiesIfPartially.required",
                "You must provide all structures within 500m");
          }
          break;
        default:
          break;
      }
    }
    ValidationUtils.rejectIfEmpty(errors, "approximateProjectLocationFromShore",
        "approximateProjectLocationFromShore.required", "You must provide approximate location information");
    if (form.getFacilitiesOffshore() == null) {
      errors.rejectValue("facilitiesOffshore", "facilitiesOffshore.required",
          "Select yes if facilities are wholly offshore and subsea");
    }
    if (form.getTransportsMaterialsToShore() == null) {
      errors.rejectValue("transportsMaterialsToShore", "transportsMaterialsToShore.required",
          "Select yes if the pipeline will be used to transport materials / facilitate the transportation of materials to shore");
    } else if (form.getTransportsMaterialsToShore().equals(true)) {
      ValidationUtils.rejectIfEmpty(errors, "transportationMethod", "transportationMethod.required",
          "You must provide the method of transportation to shore");
    } else if (form.getTransportsMaterialsToShore().equals(false)) {
      ValidationUtils.rejectIfEmpty(errors, "pipelineAshoreLocation",
          "pipelineAshoreLocation" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "You must provide the location information detailing where the pipeline comes ashore");
    }
    ValidationUtils.rejectIfEmpty(errors, "pipelineRouteDetails",
        "pipelineRouteDetails.required", "You must provide pipeline route details");
    if (form.getRouteSurveyUndertaken() == null) {
      errors.rejectValue("routeSurveyUndertaken", "routeSurveyUndertaken.required",
          "Select yes if a pipeline route survey has been undertaken");
    } else {
      if (BooleanUtils.isTrue(form.getRouteSurveyUndertaken())) {
        ValidatorUtils.validateDateIsPastOrPresent(
            "surveyConcluded", "survey concluded",
            form.getSurveyConcludedDay(),
            form.getSurveyConcludedMonth(),
            form.getSurveyConcludedYear(),
            errors
        );
      }
    }
    ValidatorUtils.validateBooleanTrue(errors,
        form.getWithinLimitsOfDeviation(),
        "withinLimitsOfDeviation",
        "You must confirm that the limit of deviation during construction will be ±100m");
  }

  public void validatePartial(Object target, Errors errors) {
    var form = (LocationDetailsForm) target;
    if (form.getRouteSurveyUndertaken() != null && form.getRouteSurveyUndertaken().equals(true)) {
      if (!(form.getSurveyConcludedDay() == null && form.getSurveyConcludedMonth() == null && form.getSurveyConcludedYear() == null)) {
        ValidatorUtils.validateDate(
            "surveyConcluded", "survey concluded",
            form.getSurveyConcludedDay(),
            form.getSurveyConcludedMonth(),
            form.getSurveyConcludedYear(),
            errors
        );
      }
    }
  }
}
