package uk.co.ogauthority.pwa.service.pwaapplications.shared.location;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.enums.LocationDetailsQuestion;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.LocationDetailsView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadLocationDetailsRepository;
import uk.co.ogauthority.pwa.service.devuk.DevukFacilityService;
import uk.co.ogauthority.pwa.service.devuk.PadFacilityService;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.validators.LocationDetailsFormValidationHints;
import uk.co.ogauthority.pwa.validators.LocationDetailsValidator;

@Service
public class PadLocationDetailsService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PadLocationDetailsService.class);

  private final PadLocationDetailsRepository padLocationDetailsRepository;
  private final PadFacilityService padFacilityService;
  private final DevukFacilityService devukFacilityService;
  private final LocationDetailsValidator validator;
  private final SearchSelectorService searchSelectorService;
  private final EntityCopyingService entityCopyingService;
  private final PadFileService padFileService;

  @Autowired
  public PadLocationDetailsService(PadLocationDetailsRepository padLocationDetailsRepository,
                                   PadFacilityService padFacilityService,
                                   DevukFacilityService devukFacilityService,
                                   LocationDetailsValidator validator,
                                   SearchSelectorService searchSelectorService,
                                   EntityCopyingService entityCopyingService,
                                   PadFileService padFileService) {
    this.padLocationDetailsRepository = padLocationDetailsRepository;
    this.padFacilityService = padFacilityService;
    this.devukFacilityService = devukFacilityService;
    this.validator = validator;
    this.searchSelectorService = searchSelectorService;
    this.entityCopyingService = entityCopyingService;
    this.padFileService = padFileService;
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
    locationDetailsForm.setRouteSurveyNotUndertakenReason(padLocationDetails.getRouteSurveyNotUndertakenReason());
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
      padLocationDetails.setRouteSurveyNotUndertakenReason(locationDetailsForm.getRouteSurveyNotUndertakenReason());
    }
    padLocationDetails.setRouteSurveyUndertaken(locationDetailsForm.getRouteSurveyUndertaken());
    padLocationDetails.setWithinLimitsOfDeviation(locationDetailsForm.getWithinLimitsOfDeviation());
    save(padLocationDetails);
  }

  public LocationDetailsView getLocationDetailsView(PwaApplicationDetail pwaApplicationDetail) {

    var locationDetails = getLocationDetailsForDraft(pwaApplicationDetail);
    var surveyConcludedTimestamp = locationDetails.getSurveyConcludedTimestamp();

    List<String> facilityNames =
        !(HseSafetyZone.NO).equals(locationDetails.getWithinSafetyZone()) ? getFacilityNames(pwaApplicationDetail) : List.of();

    List<UploadedFileView> uploadedFileViews = padFileService.getUploadedFileViews(
        pwaApplicationDetail, ApplicationDetailFilePurpose.LOCATION_DETAILS, ApplicationFileLinkStatus.FULL);

    return new LocationDetailsView(
        locationDetails.getApproximateProjectLocationFromShore(),
        locationDetails.getWithinSafetyZone(),
        HseSafetyZone.YES.equals(locationDetails.getWithinSafetyZone()) ? facilityNames : List.of(),
        HseSafetyZone.PARTIALLY.equals(locationDetails.getWithinSafetyZone()) ? facilityNames : List.of(),
        locationDetails.getFacilitiesOffshore(),
        locationDetails.getTransportsMaterialsToShore(),
        locationDetails.getTransportationMethod(),
        locationDetails.getPipelineRouteDetails(),
        locationDetails.getRouteSurveyUndertaken(),
        locationDetails.getRouteSurveyNotUndertakenReason(),
        locationDetails.getWithinLimitsOfDeviation(),
        surveyConcludedTimestamp != null ? DateUtils.formatDate(surveyConcludedTimestamp) : null,
        locationDetails.getPipelineAshoreLocation(),
        uploadedFileViews);
  }

  private List<String> getFacilityNames(PwaApplicationDetail pwaApplicationDetail) {
    var facilities = padFacilityService.getFacilities(pwaApplicationDetail);

    return facilities.stream()
        .map(padFacility -> {
          if (padFacility.isLinkedToDevukFacility()) {
            return String.valueOf(padFacility.getFacility().getFacilityName());
          } else {
            return padFacility.getFacilityNameManualEntry();
          }
        })
        .sorted()
        .collect(Collectors.toList());
  }

  public Set<LocationDetailsQuestion> getRequiredQuestions(PwaApplicationType pwaApplicationType) {

    if (pwaApplicationType == PwaApplicationType.DEPOSIT_CONSENT) {
      return Set.of(
          LocationDetailsQuestion.APPROXIMATE_PROJECT_LOCATION_FROM_SHORE,
          LocationDetailsQuestion.WITHIN_SAFETY_ZONE
      );
    }

    return EnumSet.allOf(LocationDetailsQuestion.class);
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
    var validationHints = new LocationDetailsFormValidationHints(
        ValidationType.FULL,
        getRequiredQuestions(detail.getPwaApplicationType())
    );
    validator.validate(locationDetailsForm, bindingResult, validationHints);

    return !bindingResult.hasErrors();

  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    var validationHints = new LocationDetailsFormValidationHints(
        validationType, getRequiredQuestions(pwaApplicationDetail.getPwaApplicationType()));

    validator.validate(form, bindingResult, validationHints);
    return bindingResult;
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var locationDetails = getLocationDetailsForDraft(detail);
    var requiredQuestions = getRequiredQuestions(detail.getPwaApplicationType());

    // if no to HSE safety zone, clear facilities
    if (locationDetails.getWithinSafetyZone().equals(HseSafetyZone.NO)) {
      padFacilityService.setFacilities(detail, new LocationDetailsForm());
    }

    // if all offshore/subsea, clear ashore location
    if (requiredQuestions.contains(LocationDetailsQuestion.FACILITIES_OFFSHORE) && locationDetails.getFacilitiesOffshore()) {
      locationDetails.setPipelineAshoreLocation(null);
    }

    // if not transporting materials to shore, clear transportation method
    if (requiredQuestions.contains(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)
        && !locationDetails.getTransportsMaterialsToShore()) {
      locationDetails.setTransportationMethod(null);
    }

    // if no to route survey, clear survey date
    if (requiredQuestions.contains(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN) && !locationDetails.getRouteSurveyUndertaken()) {
      locationDetails.setSurveyConcludedTimestamp(null);
    }

    save(locationDetails);

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    var duplicatePadLocationDetailsEntity = entityCopyingService.duplicateEntityAndSetParent(
        () -> getLocationDetailsForDraft(fromDetail),
        toDetail,
        PadLocationDetails.class
    );

    var duplicatedPadFacilityEntityIds = entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padFacilityService.getFacilities(fromDetail),
        toDetail,
        PadFacility.class
    );

    padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail,
        toDetail,
        ApplicationDetailFilePurpose.LOCATION_DETAILS,
        ApplicationFileLinkStatus.FULL
    );

  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return !pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION);
  }

}
