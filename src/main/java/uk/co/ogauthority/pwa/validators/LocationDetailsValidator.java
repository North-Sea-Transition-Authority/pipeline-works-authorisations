package uk.co.ogauthority.pwa.validators;

import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.enums.LocationDetailsQuestion;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class LocationDetailsValidator implements SmartValidator {

  private final LocationDetailsSafetyZoneValidator safetyZoneValidator;


  @Autowired
  public LocationDetailsValidator(
      LocationDetailsSafetyZoneValidator safetyZoneValidator) {
    this.safetyZoneValidator = safetyZoneValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(LocationDetailsForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw(new ActionNotAllowedException("Incorrect parameters provided for validation"));
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (LocationDetailsForm) target;
    var locationDetailsValidationHints = (LocationDetailsFormValidationHints) validationHints[0];
    var requiredQuestions = locationDetailsValidationHints.getRequiredQuestions();

    validatePartial(form, errors, requiredQuestions);
    if (locationDetailsValidationHints.getValidationType().equals(ValidationType.FULL)) {
      validateFull(form, errors, requiredQuestions);
    }

  }



  private void validatePartial(LocationDetailsForm form, Errors errors, Set<LocationDetailsQuestion> requiredQuestions) {

    if (requiredQuestions.contains(LocationDetailsQuestion.APPROXIMATE_PROJECT_LOCATION_FROM_SHORE)) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "approximateProjectLocationFromShore", form::getApproximateProjectLocationFromShore,
          "Approximate project location from shore must be 4000 characters or fewer");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "transportationMethod", form::getTransportationMethod,
          "Transportation method must be 4000 characters or fewer");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.FACILITIES_OFFSHORE)) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "pipelineAshoreLocation", form::getPipelineAshoreLocation,
          "Pipeline ashore location must be 4000 characters or fewer");
    }


    if (requiredQuestions.contains(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)) {
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
  }


  private void validateFull(LocationDetailsForm form, Errors errors, Set<LocationDetailsQuestion> requiredQuestions) {


    if (requiredQuestions.contains(LocationDetailsQuestion.APPROXIMATE_PROJECT_LOCATION_FROM_SHORE)) {
      ValidationUtils.rejectIfEmpty(errors, "approximateProjectLocationFromShore",
          "approximateProjectLocationFromShore.required", "Enter approximate location information");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          safetyZoneValidator,
          "safetyZoneQuestionForm",
          form.getSafetyZoneQuestionForm());
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.FACILITIES_OFFSHORE)) {
      if (form.getFacilitiesOffshore() == null) {
        errors.rejectValue("facilitiesOffshore", "facilitiesOffshore.required",
            "Select yes if facilities are wholly offshore and subsea");
      } else if (form.getFacilitiesOffshore().equals(false)) {
        ValidationUtils.rejectIfEmpty(errors, "pipelineAshoreLocation",
            "pipelineAshoreLocation" + FieldValidationErrorCodes.REQUIRED.getCode(),
            "Enter the location information detailing where the pipeline comes ashore");
      }
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)) {
      if (form.getTransportsMaterialsToShore() == null) {
        errors.rejectValue("transportsMaterialsToShore", "transportsMaterialsToShore.required",
            "Select yes if the pipeline will be used to transport materials / facilitate the transportation of materials to shore");
      } else if (form.getTransportsMaterialsToShore().equals(true)) {
        ValidationUtils.rejectIfEmpty(errors, "transportationMethod", "transportationMethod.required",
            "Enter the method of transportation to shore");
      }
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)) {
      if (form.getRouteSurveyUndertaken() == null) {
        errors.rejectValue("routeSurveyUndertaken", "routeSurveyUndertaken.required",
            "Select yes if a pipeline route survey has been undertaken");
      } else {
        if (BooleanUtils.isTrue(form.getRouteSurveyUndertaken())) {
          ValidationUtils.rejectIfEmpty(errors, "pipelineRouteDetails",
              "pipelineRouteDetails.required", "Enter pipeline route details");
        }
      }
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.WITHIN_LIMITS_OF_DEVIATION)) {
      ValidatorUtils.validateBooleanTrue(errors,
          form.getWithinLimitsOfDeviation(),
          "withinLimitsOfDeviation",
          "Confirm that the limit of deviation during construction will be ±100m");
    }
  }






}
