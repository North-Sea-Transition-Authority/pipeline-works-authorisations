package uk.co.ogauthority.pwa.service.pwaapplications.shared.location;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadLocationDetailsRepository;
import uk.co.ogauthority.pwa.service.devuk.DevukFacilityService;
import uk.co.ogauthority.pwa.service.devuk.PadFacilityService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.LocationDetailsValidator;

@Service
public class PadLocationDetailsService implements ApplicationFormSectionService {

  private final PadLocationDetailsRepository padLocationDetailsRepository;
  private final PadFacilityService padFacilityService;
  private final DevukFacilityService devukFacilityService;
  private final LocationDetailsValidator validator;
  private final SpringValidatorAdapter groupValidator;
  private final SearchSelectorService searchSelectorService;

  @Autowired
  public PadLocationDetailsService(PadLocationDetailsRepository padLocationDetailsRepository,
                                   PadFacilityService padFacilityService,
                                   DevukFacilityService devukFacilityService,
                                   LocationDetailsValidator validator,
                                   SpringValidatorAdapter groupValidator,
                                   SearchSelectorService searchSelectorService) {
    this.padLocationDetailsRepository = padLocationDetailsRepository;
    this.padFacilityService = padFacilityService;
    this.devukFacilityService = devukFacilityService;
    this.validator = validator;
    this.groupValidator = groupValidator;
    this.searchSelectorService = searchSelectorService;
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
      padLocationDetails.setPipelineRouteDetails(locationDetailsForm.getPipelineRouteDetails());
    } else {
      padLocationDetails.setSurveyConcludedTimestamp(null);
      padLocationDetails.setPipelineRouteDetails(null);
    }
    padLocationDetails.setRouteSurveyUndertaken(locationDetailsForm.getRouteSurveyUndertaken());
    padLocationDetails.setWithinLimitsOfDeviation(locationDetailsForm.getWithinLimitsOfDeviation());
    save(padLocationDetails);
  }

  public Map<String, String> reapplyFacilitySelections(LocationDetailsForm form) {
    List<String> facilities = List.of();
    if (form.getWithinSafetyZone() == HseSafetyZone.PARTIALLY) {
      facilities = form.getFacilitiesIfPartially();
    } else if (form.getWithinSafetyZone() == HseSafetyZone.YES) {
      facilities = form.getFacilitiesIfYes();
    }

    List<DevukFacility> devukFacilities = List.of();
    if (facilities.size() > 0) {
      devukFacilities = devukFacilityService.getFacilitiesInIds(facilities);
    }

    Map<String, String> resolveMap = devukFacilities.stream()
        .collect(StreamUtils.toLinkedHashMap(devukFacility -> String.valueOf(devukFacility.getId()),
            DevukFacility::getFacilityName));

    return searchSelectorService.buildPrepopulatedSelections(facilities, resolveMap);
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

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var locationDetails = getLocationDetailsForDraft(detail);

    // if no to HSE safety zone, clear facilities
    if (locationDetails.getWithinSafetyZone().equals(HseSafetyZone.NO)) {
      padFacilityService.setFacilities(detail, new LocationDetailsForm());
    }

    // if all offshore/subsea, clear ashore location
    if (locationDetails.getFacilitiesOffshore()) {
      locationDetails.setPipelineAshoreLocation(null);
    }

    // if not transporting materials to shore, clear transportation method
    if (!locationDetails.getTransportsMaterialsToShore()) {
      locationDetails.setTransportationMethod(null);
    }

    // if no to route survey, clear survey date
    if (!locationDetails.getRouteSurveyUndertaken()) {
      locationDetails.setSurveyConcludedTimestamp(null);
    }

    save(locationDetails);

  }
}
