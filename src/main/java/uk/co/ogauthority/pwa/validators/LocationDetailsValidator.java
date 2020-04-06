package uk.co.ogauthority.pwa.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.LocationDetailsForm;

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
    if (StringUtils.isBlank(form.getApproximateProjectLocationFromShore())) {
      errors.rejectValue("approximateProjectLocationFromShore", "approximateProjectLocationFromShore.required",
          "You must provide approximate location information");
    }
    if (form.getFacilitiesOffshore() == null) {
      errors.rejectValue("facilitiesOffshore", "facilitiesOffshore.required",
          "Select yes if facilities are wholly offshore and subsea");
    }
    if (form.getTransportsMaterialsToShore() == null) {
      errors.rejectValue("transportsMaterialsToShore", "transportsMaterialsToShore.required",
          "Select yes if the pipeline will be used to transport materials / facilitate the transportation of materials to shore");
    } else if (form.getTransportsMaterialsToShore().equals(true) && StringUtils.isBlank(form.getTransportationMethod())) {
      errors.rejectValue("transportationMethod", "transportationMethod.required",
          "You must provide the method of transportation to shore");
    }
  }
}
