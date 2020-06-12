package uk.co.ogauthority.pwa.service.pwaapplications.shared.location;

import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadLocationDetailsRepository;
import uk.co.ogauthority.pwa.service.devuk.PadFacilityService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.LocationDetailsValidator;

@Service
public class PadLocationDetailsService implements ApplicationFormSectionService {

  private final PadLocationDetailsRepository padLocationDetailsRepository;
  private final PadFacilityService padFacilityService;
  private final LocationDetailsValidator validator;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public PadLocationDetailsService(PadLocationDetailsRepository padLocationDetailsRepository,
                                   PadFacilityService padFacilityService,
                                   LocationDetailsValidator validator,
                                   SpringValidatorAdapter groupValidator) {
    this.padLocationDetailsRepository = padLocationDetailsRepository;
    this.padFacilityService = padFacilityService;
    this.validator = validator;
    this.groupValidator = groupValidator;
  }

  public PadLocationDetails getLocationDetailsForDraft(PwaApplicationDetail detail) {
    var locationDetailIfOptionalEmpty = new PadLocationDetails();
    locationDetailIfOptionalEmpty.setPwaApplicationDetail(detail);
    return padLocationDetailsRepository.findByPwaApplicationDetail(detail)
        .orElse(locationDetailIfOptionalEmpty);
  }

  @Transactional
  public void save(PadLocationDetails padLocationDetails) {
    padLocationDetailsRepository.save(padLocationDetails);
  }

  public void mapEntityToForm(PadLocationDetails padLocationDetails, LocationDetailsForm locationDetailsForm) {
    locationDetailsForm.setApproximateProjectLocationFromShore(
        padLocationDetails.getApproximateProjectLocationFromShore());
    locationDetailsForm.setWithinSafetyZone(padLocationDetails.getWithinSafetyZone());
    locationDetailsForm.setFacilitiesOffshore(padLocationDetails.getFacilitiesOffshore());
    locationDetailsForm.setTransportsMaterialsToShore(padLocationDetails.getTransportsMaterialsToShore());
    locationDetailsForm.setTransportationMethod(padLocationDetails.getTransportationMethod());
    locationDetailsForm.setPipelineRouteDetails(padLocationDetails.getPipelineRouteDetails());
    locationDetailsForm.setPipelineAshoreLocation(padLocationDetails.getPipelineAshoreLocation());
    DateUtils.setYearMonthDayFromInstant(
        locationDetailsForm::setSurveyConcludedYear,
        locationDetailsForm::setSurveyConcludedMonth,
        locationDetailsForm::setSurveyConcludedDay,
        padLocationDetails.getSurveyConcludedTimestamp()
    );
    locationDetailsForm.setRouteSurveyUndertaken(padLocationDetails.getRouteSurveyUndertaken());
    locationDetailsForm.setWithinLimitsOfDeviation(padLocationDetails.getWithinLimitsOfDeviation());
  }

  @Transactional
  public void saveEntityUsingForm(PadLocationDetails padLocationDetails, LocationDetailsForm locationDetailsForm) {
    padLocationDetails.setApproximateProjectLocationFromShore(
        locationDetailsForm.getApproximateProjectLocationFromShore());
    padLocationDetails.setWithinSafetyZone(locationDetailsForm.getWithinSafetyZone());
    padLocationDetails.setFacilitiesOffshore(locationDetailsForm.getFacilitiesOffshore());
    padLocationDetails.setTransportsMaterialsToShore(locationDetailsForm.getTransportsMaterialsToShore());
    padLocationDetails.setTransportationMethod(locationDetailsForm.getTransportationMethod());
    padLocationDetails.setPipelineRouteDetails(locationDetailsForm.getPipelineRouteDetails());
    if (BooleanUtils.isFalse(locationDetailsForm.getFacilitiesOffshore())) {
      padLocationDetails.setPipelineAshoreLocation(locationDetailsForm.getPipelineAshoreLocation());
    } else {
      padLocationDetails.setPipelineAshoreLocation(null);
    }
    if (BooleanUtils.isTrue(locationDetailsForm.getRouteSurveyUndertaken())) {
      DateUtils.consumeInstantFromIntegersElseNull(
          locationDetailsForm.getSurveyConcludedYear(),
          locationDetailsForm.getSurveyConcludedMonth(),
          locationDetailsForm.getSurveyConcludedDay(),
          padLocationDetails::setSurveyConcludedTimestamp
      );
    } else {
      padLocationDetails.setSurveyConcludedTimestamp(null);
    }
    padLocationDetails.setRouteSurveyUndertaken(locationDetailsForm.getRouteSurveyUndertaken());
    padLocationDetails.setWithinLimitsOfDeviation(locationDetailsForm.getWithinLimitsOfDeviation());
    save(padLocationDetails);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {

    PadLocationDetails locationDetails = getLocationDetailsForDraft(detail);
    var locationDetailsForm = new LocationDetailsForm();
    mapEntityToForm(locationDetails, locationDetailsForm);

    var facilities = padFacilityService.getFacilities(detail);

    var formFacilities = facilities.stream()
        .map(padFacility -> {
          if (padFacility.isLinkedToDevukFacility()) {
            return String.valueOf(padFacility.getFacility().getId());
          } else {
            return padFacility.getFacilityNameManualEntry();
          }
        })
        .collect(Collectors.toList());

    // if facilities exist and we're near a safety zone, add to form
    if (!facilities.isEmpty() && !locationDetails.getWithinSafetyZone().equals(HseSafetyZone.NO)) {

      if (locationDetails.getWithinSafetyZone().equals(HseSafetyZone.YES)) {
        locationDetailsForm.setFacilitiesIfYes(formFacilities);
      } else if (locationDetails.getWithinSafetyZone().equals(HseSafetyZone.PARTIALLY)) {
        locationDetailsForm.setFacilitiesIfPartially(formFacilities);
      }

    }

    BindingResult bindingResult = new BeanPropertyBindingResult(locationDetailsForm, "form");
    validator.validate(locationDetailsForm, bindingResult);

    return !bindingResult.hasErrors();

  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    if (validationType.equals(ValidationType.PARTIAL)) {
      groupValidator.validate(form, bindingResult, PartialValidation.class);
      validator.validatePartial(form, bindingResult);
      return bindingResult;
    }

    groupValidator.validate(form, bindingResult, PartialValidation.class);
    validator.validate(form, bindingResult);
    return bindingResult;

  }
}
