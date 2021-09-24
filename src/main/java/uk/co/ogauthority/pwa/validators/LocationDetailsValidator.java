package uk.co.ogauthority.pwa.validators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.enums.LocationDetailsQuestion;
import uk.co.ogauthority.pwa.model.entity.enums.locationdetails.PsrNotification;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.ValidatorUtils;
import uk.co.ogauthority.pwa.util.forminputs.FormInputLabel;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrBeforeDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@Service
public class LocationDetailsValidator implements SmartValidator {

  private final LocationDetailsSafetyZoneValidator safetyZoneValidator;
  private final TwoFieldDateInputValidator twoFieldDateInputValidator;


  @Autowired
  public LocationDetailsValidator(
      LocationDetailsSafetyZoneValidator safetyZoneValidator,
      TwoFieldDateInputValidator twoFieldDateInputValidator) {
    this.safetyZoneValidator = safetyZoneValidator;
    this.twoFieldDateInputValidator = twoFieldDateInputValidator;
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
          "Approximate project location from shore");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)) {
      validateWithinSafetyZone(form, errors, ValidationType.PARTIAL);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.PSR_NOTIFICATION)) {
      if (PsrNotification.YES.equals(form.getPsrNotificationSubmittedOption())) {
        ValidatorUtils.invokeNestedValidator(
            errors,
            twoFieldDateInputValidator,
            "psrNotificationSubmittedDate",
            form.getPsrNotificationSubmittedDate(),
            List.of(new FormInputLabel("submitted")).toArray());

      } else if (PsrNotification.NO.equals(form.getPsrNotificationSubmittedOption())) {
        ValidatorUtils.invokeNestedValidator(
            errors,
            twoFieldDateInputValidator,
            "psrNotificationExpectedSubmissionDate",
            form.getPsrNotificationExpectedSubmissionDate(),
            List.of(new FormInputLabel("expected submission")).toArray());

      } else if (PsrNotification.NOT_REQUIRED.equals(form.getPsrNotificationSubmittedOption())) {
        validatePsrNotificationNotRequired(form, errors);
      }
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "transportationMethod", form::getTransportationMethod,
          "Transportation method");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.FACILITIES_OFFSHORE)) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "pipelineAshoreLocation", form::getPipelineAshoreLocation,
          "Pipeline ashore location");
    }


    if (requiredQuestions.contains(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)) {

      if (BooleanUtils.isTrue(form.getRouteSurveyUndertaken())) {
        ValidatorUtils.validateDateIsPastOrPresent(
            "surveyConcluded", "survey concluded",
            form.getSurveyConcludedDay(),
            form.getSurveyConcludedMonth(),
            form.getSurveyConcludedYear(),
            errors);

        ValidatorUtils.validateDefaultStringLength(
            errors, "pipelineRouteDetails", form::getPipelineRouteDetails,
            "Pipeline route details");

      } else if (BooleanUtils.isFalse(form.getRouteSurveyUndertaken())) {
        ValidatorUtils.validateDefaultStringLength(
            errors, "routeSurveyNotUndertakenReason", form::getRouteSurveyNotUndertakenReason,
            "The reason for why a pipeline route survey has not been undertaken");
      }
    }

  }


  private void validateFull(LocationDetailsForm form, Errors errors, Set<LocationDetailsQuestion> requiredQuestions) {

    if (requiredQuestions.contains(LocationDetailsQuestion.APPROXIMATE_PROJECT_LOCATION_FROM_SHORE)) {
      ValidationUtils.rejectIfEmpty(errors, "approximateProjectLocationFromShore",
          "approximateProjectLocationFromShore.required", "Enter approximate location information");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)) {
      validateWithinSafetyZone(form, errors, ValidationType.FULL);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.PSR_NOTIFICATION)) {
      ValidationUtils.rejectIfEmpty(errors, "psrNotificationSubmittedOption",
          "psrNotificationSubmittedOption" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select 'Yes' if you have submitted a Pipelines Safety Regulations notification to HSE");

      if (PsrNotification.YES.equals(form.getPsrNotificationSubmittedOption())) {
        List<Object> dateHints = new ArrayList<>();
        dateHints.add(new FormInputLabel("submitted"));
        dateHints.add(new OnOrBeforeDateHint(LocalDate.now(), "today's date"));

        ValidatorUtils.invokeNestedValidator(
            errors,
            twoFieldDateInputValidator,
            "psrNotificationSubmittedDate",
            form.getPsrNotificationSubmittedDate(),
            dateHints.toArray());

      } else if (PsrNotification.NO.equals(form.getPsrNotificationSubmittedOption())) {
        List<Object> dateHints = new ArrayList<>();
        dateHints.add(new FormInputLabel("expected submission"));
        dateHints.add(new OnOrAfterDateHint(LocalDate.now(), "today's date"));

        ValidatorUtils.invokeNestedValidator(
            errors,
            twoFieldDateInputValidator,
            "psrNotificationExpectedSubmissionDate",
            form.getPsrNotificationExpectedSubmissionDate(),
            dateHints.toArray());

      } else if (PsrNotification.NOT_REQUIRED.equals(form.getPsrNotificationSubmittedOption())) {
        validatePsrNotificationNotRequired(form, errors);
      }
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.DIVERS_USED)) {
      ValidationUtils.rejectIfEmpty(errors, "diversUsed",
          "diversUsed" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select 'Yes' if divers will be used");
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
        } else {
          ValidationUtils.rejectIfEmpty(errors, "routeSurveyNotUndertakenReason",
              "routeSurveyNotUndertakenReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
              "Enter the reason for why a pipeline route survey has not been undertaken");
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

  private void validatePsrNotificationNotRequired(LocationDetailsForm form, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "psrNotificationNotRequiredReason",
        "psrNotificationNotRequiredReason" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Enter a reason for why a PSR notification is not required");

    if (form.getPsrNotificationNotRequiredReason() != null) {
      ValidatorUtils.validateDefaultStringLength(
          errors, "psrNotificationNotRequiredReason", form::getPsrNotificationNotRequiredReason,
          "The reason for why a PSR notification is not required");
    }
  }

  private void validateWithinSafetyZone(LocationDetailsForm form,
                                        Errors errors,
                                        ValidationType validationType) {

    if (validationType.equals(ValidationType.FULL) && form.getWithinSafetyZone() == null) {
      errors.rejectValue("withinSafetyZone",
          "withinSafetyZone" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Enter information on work carried out within 500m of a safety zone");

    } else if (form.getWithinSafetyZone() != null) {

      switch (form.getWithinSafetyZone()) {
        case YES:
          ValidatorUtils.invokeNestedValidator(
              errors,
              safetyZoneValidator,
              "completelyWithinSafetyZoneForm",
              form.getCompletelyWithinSafetyZoneForm(),
              validationType);
          break;
        case PARTIALLY:
          ValidatorUtils.invokeNestedValidator(
              errors,
              safetyZoneValidator,
              "partiallyWithinSafetyZoneForm",
              form.getPartiallyWithinSafetyZoneForm(),
              validationType);
          break;
        default:
          break;
      }

    }

  }

}
