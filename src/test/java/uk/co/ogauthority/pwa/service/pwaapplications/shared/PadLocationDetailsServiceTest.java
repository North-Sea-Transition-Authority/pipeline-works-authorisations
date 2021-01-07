package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.enums.LocationDetailsQuestion;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadLocationDetailsRepository;
import uk.co.ogauthority.pwa.service.devuk.DevukFacilityService;
import uk.co.ogauthority.pwa.service.devuk.PadFacilityService;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailsService;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.LocationDetailsValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadLocationDetailsServiceTest {

  @Mock
  private PadLocationDetailsRepository padLocationDetailsRepository;

  @Mock
  private PadFacilityService facilityService;

  @Mock
  private DevukFacilityService devukFacilityService;

  @Mock
  private SearchSelectorService searchSelectorService;

  @Mock
  private LocationDetailsValidator validator;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Mock
  private PadFileService padFileService;


  private PadLocationDetailsService padLocationDetailsService;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadLocationDetails padLocationDetails;
  private static final Instant SURVEY_CONCLUDED_DATE = LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);

  @Before
  public void setUp() {

    padLocationDetailsService = new PadLocationDetailsService(
        padLocationDetailsRepository,
        facilityService,
        devukFacilityService,
        validator,
        searchSelectorService,
        entityCopyingService,
        padFileService);

    pwaApplicationDetail = new PwaApplicationDetail();
    padLocationDetails = buildEntity();
  }

  @Test
  public void getLocationDetailsForDraft_WhenNull() {
    when(padLocationDetailsRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.empty());
    var result = padLocationDetailsService.getLocationDetailsForDraft(pwaApplicationDetail);
    assertThat(result).isNotEqualTo(padLocationDetails);
    assertThat(result.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void getLocationDetailsForDraft_WhenExists() {
    when(padLocationDetailsRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(padLocationDetails));
    var result = padLocationDetailsService.getLocationDetailsForDraft(pwaApplicationDetail);
    assertThat(result).isEqualTo(padLocationDetails);
  }

  @Test
  public void save() {
    padLocationDetailsService.save(padLocationDetails);
    verify(padLocationDetailsRepository, times(1)).save(padLocationDetails);
  }

  @Test
  public void mapEntityToForm_WithNulls() {
    var form = new LocationDetailsForm();
    var entity = new PadLocationDetails();
    padLocationDetailsService.mapEntityToForm(entity, form);
    assertThat(form.getWithinSafetyZone()).isNull();
    assertThat(form.getApproximateProjectLocationFromShore()).isNull();
    assertThat(form.getFacilitiesOffshore()).isNull();
    assertThat(form.getTransportsMaterialsToShore()).isNull();
    assertThat(form.getTransportationMethod()).isNull();
    assertThat(form.getPipelineRouteDetails()).isNull();
    assertThat(form.getSurveyConcludedDay()).isNull();
    assertThat(form.getSurveyConcludedMonth()).isNull();
    assertThat(form.getSurveyConcludedYear()).isNull();
    assertThat(form.getRouteSurveyUndertaken()).isNull();
    assertThat(form.getRouteSurveyNotUndertakenReason()).isNull();
    assertThat(form.getWithinLimitsOfDeviation()).isNull();
  }

  @Test
  public void mapEntityToForm_WithData() {
    var form = new LocationDetailsForm();
    padLocationDetails.setWithinSafetyZone(HseSafetyZone.NO);
    padLocationDetailsService.mapEntityToForm(padLocationDetails, form);
    assertThat(form.getWithinSafetyZone()).isEqualTo(padLocationDetails.getWithinSafetyZone());
    assertThat(form.getApproximateProjectLocationFromShore()).isEqualTo(
        padLocationDetails.getApproximateProjectLocationFromShore());
    assertThat(form.getFacilitiesOffshore()).isEqualTo(padLocationDetails.getFacilitiesOffshore());
    assertThat(form.getTransportsMaterialsToShore()).isEqualTo(padLocationDetails.getTransportsMaterialsToShore());
    assertThat(form.getTransportationMethod()).isEqualTo(padLocationDetails.getTransportationMethod());
    assertThat(form.getPipelineRouteDetails()).isEqualTo(padLocationDetails.getPipelineRouteDetails());
    assertThat(form.getRouteSurveyUndertaken()).isEqualTo(padLocationDetails.getRouteSurveyUndertaken());
    assertThat(form.getWithinLimitsOfDeviation()).isEqualTo(padLocationDetails.getWithinLimitsOfDeviation());
    var localDate = LocalDate.ofInstant(padLocationDetails.getSurveyConcludedTimestamp(), ZoneId.systemDefault());
    assertThat(form.getSurveyConcludedYear()).isEqualTo(localDate.getYear());
    assertThat(form.getSurveyConcludedMonth()).isEqualTo(localDate.getMonthValue());
    assertThat(form.getSurveyConcludedDay()).isEqualTo(localDate.getDayOfMonth());
  }

  @Test
  public void saveEntityUsingForm_WithNulls() {
    var form = new LocationDetailsForm();
    var entity = new PadLocationDetails();
    padLocationDetailsService.saveEntityUsingForm(entity, form);
    assertThat(entity.getWithinSafetyZone()).isNull();
    assertThat(entity.getApproximateProjectLocationFromShore()).isNull();
    assertThat(entity.getFacilitiesOffshore()).isNull();
    assertThat(entity.getTransportsMaterialsToShore()).isNull();
    assertThat(entity.getTransportsMaterialsToShore()).isNull();
    assertThat(entity.getPipelineRouteDetails()).isNull();
    assertThat(entity.getSurveyConcludedTimestamp()).isNull();
    assertThat(entity.getRouteSurveyUndertaken()).isNull();
    assertThat(entity.getRouteSurveyNotUndertakenReason()).isNull();
    assertThat(entity.getWithinLimitsOfDeviation()).isNull();
  }

  @Test
  public void saveEntityUsingForm_WithData() {
    var form = buildForm();
    var entity = new PadLocationDetails();
    padLocationDetailsService.saveEntityUsingForm(entity, form);
    assertThat(entity.getWithinSafetyZone()).isEqualTo(form.getWithinSafetyZone());
    assertThat(entity.getApproximateProjectLocationFromShore()).isEqualTo(
        form.getApproximateProjectLocationFromShore());
    assertThat(entity.getFacilitiesOffshore()).isEqualTo(form.getFacilitiesOffshore());
    assertThat(entity.getTransportsMaterialsToShore()).isEqualTo(form.getTransportsMaterialsToShore());
    assertThat(entity.getTransportationMethod()).isEqualTo(form.getTransportationMethod());
    assertThat(entity.getPipelineRouteDetails()).isEqualTo(form.getPipelineRouteDetails());
    assertThat(entity.getSurveyConcludedTimestamp()).isEqualTo(SURVEY_CONCLUDED_DATE);
    assertThat(entity.getRouteSurveyUndertaken()).isEqualTo(form.getRouteSurveyUndertaken());
    assertThat(entity.getWithinLimitsOfDeviation()).isEqualTo(form.getWithinLimitsOfDeviation());
  }

  @Test
  public void saveEntityUsingForm_ashoreLocation_saved() {
    var form = new LocationDetailsForm();
    form.setFacilitiesOffshore(false);
    form.setPipelineAshoreLocation("test");
    var entity = new PadLocationDetails();
    padLocationDetailsService.saveEntityUsingForm(entity, form);
    assertThat(entity.getPipelineAshoreLocation()).isEqualTo(form.getPipelineAshoreLocation());
  }

  @Test
  public void saveEntityUsingForm_ashoreLocation_notSaved() {
    var form = new LocationDetailsForm();
    form.setFacilitiesOffshore(true);
    form.setPipelineAshoreLocation("test");
    var entity = new PadLocationDetails();
    padLocationDetailsService.saveEntityUsingForm(entity, form);
    assertThat(entity.getPipelineAshoreLocation()).isEqualTo(null);
  }

  @Test
  public void saveEntityUsingForm_routeSurveyUndertakenAnsweredNo() {
    var form = new LocationDetailsForm();
    form.setRouteSurveyUndertaken(false);
    form.setRouteSurveyNotUndertakenReason("test");
    var entity = new PadLocationDetails();
    padLocationDetailsService.saveEntityUsingForm(entity, form);
    assertThat(entity.getRouteSurveyNotUndertakenReason()).isEqualTo(form.getRouteSurveyNotUndertakenReason());
  }

  @Test
  public void getLocationDetailsView_withFacilitiesAndFiles() {
    padLocationDetails.setWithinSafetyZone(HseSafetyZone.YES);
    when(padLocationDetailsRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(padLocationDetails));

    var padFacility = new PadFacility();
    padFacility.setFacility(new DevukFacility(1, "Test facility"));
    when(facilityService.getFacilities(pwaApplicationDetail)).thenReturn(List.of(padFacility));

    var uploadedFileView = new UploadedFileView("1", "name", 0L, "desc", Instant.now(), "#");
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.LOCATION_DETAILS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(uploadedFileView));

    var locationDetailsView = padLocationDetailsService.getLocationDetailsView(pwaApplicationDetail);

    assertThat(locationDetailsView.getWithinSafetyZone()).isEqualTo(HseSafetyZone.YES);
    assertThat(locationDetailsView.getFacilitiesIfYes()).contains("Test facility");
    assertThat(locationDetailsView.getFacilitiesIfPartially()).isEmpty();
    assertThat(locationDetailsView.getApproximateProjectLocationFromShore()).isEqualTo("approx");
    assertThat(locationDetailsView.getFacilitiesOffshore()).isTrue();
    assertThat(locationDetailsView.getTransportsMaterialsToShore()).isTrue();
    assertThat(locationDetailsView.getTransportationMethod()).isEqualTo("method");
    assertThat(locationDetailsView.getPipelineRouteDetails()).isEqualTo("Route details");
    assertThat(locationDetailsView.getRouteSurveyUndertaken()).isTrue();
    assertThat(locationDetailsView.getWithinLimitsOfDeviation()).isTrue();
    assertThat(locationDetailsView.getSurveyConcludedDate()).isEqualTo(DateUtils.formatDate(SURVEY_CONCLUDED_DATE));
    assertThat(locationDetailsView.getUploadedLetterFileViews()).isEqualTo(List.of(uploadedFileView));
  }

  @Test
  public void getLocationDetailsView_noFacilities() {
    when(padLocationDetailsRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(padLocationDetails));

    var locationDetailsView = padLocationDetailsService.getLocationDetailsView(pwaApplicationDetail);
    assertThat(locationDetailsView.getWithinSafetyZone()).isEqualTo(HseSafetyZone.NO);
    assertThat(locationDetailsView.getFacilitiesIfYes()).isEmpty();
    assertThat(locationDetailsView.getFacilitiesIfPartially()).isEmpty();
  }

  @Test
  public void getRequiredQuestions_depconAppType() {
    var requiredQuestions = padLocationDetailsService.getRequiredQuestions(PwaApplicationType.DEPOSIT_CONSENT);
    assertThat(requiredQuestions).containsOnly(
        LocationDetailsQuestion.APPROXIMATE_PROJECT_LOCATION_FROM_SHORE,
        LocationDetailsQuestion.WITHIN_SAFETY_ZONE
    );
  }

  @Test
  public void getRequiredQuestions_allAppTypesExceptDepcon() {
    PwaApplicationType.stream().filter(appType -> !appType.equals(PwaApplicationType.DEPOSIT_CONSENT))
      .forEach(appType -> {
        var requiredQuestions = padLocationDetailsService.getRequiredQuestions(appType);
        assertThat(requiredQuestions).containsAll(EnumSet.allOf(LocationDetailsQuestion.class));
      });
  }

  @Test
  public void cleanupData_hiddenData() {

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail.setPwaApplication(pwaApplication);

    padLocationDetails.setWithinSafetyZone(HseSafetyZone.NO);

    padLocationDetails.setFacilitiesOffshore(true);
    padLocationDetails.setPipelineAshoreLocation("ashore");

    padLocationDetails.setTransportsMaterialsToShore(false);
    padLocationDetails.setTransportationMethod("transport");

    padLocationDetails.setRouteSurveyUndertaken(false);
    padLocationDetails.setSurveyConcludedTimestamp(Instant.now());

    when(padLocationDetailsRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padLocationDetails));

    padLocationDetailsService.cleanupData(pwaApplicationDetail);

    assertThat(padLocationDetails.getPipelineAshoreLocation()).isNull();

    assertThat(padLocationDetails.getTransportationMethod()).isNull();

    assertThat(padLocationDetails.getSurveyConcludedTimestamp()).isNull();

    verify(facilityService, times(1)).setFacilities(eq(pwaApplicationDetail), eq(new LocationDetailsForm()));

    verify(padLocationDetailsRepository, times(1)).save(padLocationDetails);

  }

  @Test
  public void cleanupData_noHiddenData() {

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail.setPwaApplication(pwaApplication);

    padLocationDetails.setWithinSafetyZone(HseSafetyZone.YES);

    padLocationDetails.setFacilitiesOffshore(false);
    padLocationDetails.setPipelineAshoreLocation("ashore");

    padLocationDetails.setTransportsMaterialsToShore(true);
    padLocationDetails.setTransportationMethod("transport");

    padLocationDetails.setRouteSurveyUndertaken(true);
    padLocationDetails.setSurveyConcludedTimestamp(Instant.now());

    when(padLocationDetailsRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padLocationDetails));

    padLocationDetailsService.cleanupData(pwaApplicationDetail);

    assertThat(padLocationDetails.getPipelineAshoreLocation()).isNotNull();

    assertThat(padLocationDetails.getTransportationMethod()).isNotNull();

    assertThat(padLocationDetails.getSurveyConcludedTimestamp()).isNotNull();

    verifyNoInteractions(facilityService);

    verify(padLocationDetailsRepository, times(1)).save(padLocationDetails);

  }

  @Test
  public void reapplyFacilitySelections_serviceInteraction_noSelection() {
    var form = new LocationDetailsForm();
    padLocationDetailsService.reapplyFacilitySelections(form);
    verifyNoInteractions(devukFacilityService);
    verify(searchSelectorService, times(1)).buildPrepopulatedSelections(any(), any());
  }

  @Test
  public void reapplyFacilitySelections_serviceInteraction_inSafetyZone_Yes() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.setFacilitiesIfYes(List.of("yes"));
    form.setFacilitiesIfPartially(List.of("partially"));
    padLocationDetailsService.reapplyFacilitySelections(form);
    verify(devukFacilityService, times(1)).getFacilitiesInIds(List.of("yes"));
    verify(devukFacilityService, never()).getFacilitiesInIds(List.of("partially"));
    verify(searchSelectorService, times(1)).buildPrepopulatedSelections(any(), any());
  }

  @Test
  public void reapplyFacilitySelections_serviceInteraction_inSafetyZone_Partially() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    form.setFacilitiesIfYes(List.of("yes"));
    form.setFacilitiesIfPartially(List.of("partially"));
    padLocationDetailsService.reapplyFacilitySelections(form);
    verify(devukFacilityService, times(1)).getFacilitiesInIds(List.of("partially"));
    verify(devukFacilityService, never()).getFacilitiesInIds(List.of("yes"));
    verify(searchSelectorService, times(1)).buildPrepopulatedSelections(any(), any());
  }

  @Test
  public void reapplyFacilitySelections_fullRun() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.setFacilitiesIfYes(List.of("1", SearchSelectable.FREE_TEXT_PREFIX + "yes"));

    var devukFacility = new DevukFacility(1, "Test facility");
    when(devukFacilityService.getFacilitiesInIds(form.getFacilitiesIfYes())).thenReturn(List.of(devukFacility));
    when(searchSelectorService.buildPrepopulatedSelections(any(), any())).thenCallRealMethod();
    when(searchSelectorService.removePrefix(any())).thenCallRealMethod();
    var result = padLocationDetailsService.reapplyFacilitySelections(form);
    assertThat(result).contains(
      entry("1", "Test facility"),
      entry(SearchSelectable.FREE_TEXT_PREFIX + "yes", "yes")
    );
  }

  @Test
  public void canShowInTaskList_allowed() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    detail.setPwaApplication(app);

    PwaApplicationType.stream()
        .filter(type -> !type.equals(PwaApplicationType.OPTIONS_VARIATION))
        .forEach(applicationType -> {

          app.setApplicationType(applicationType);

          assertThat(padLocationDetailsService.canShowInTaskList(detail)).isTrue();

        });

  }

  @Test
  public void canShowInTaskList_notAllowed() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    detail.setPwaApplication(app);

    assertThat(padLocationDetailsService.canShowInTaskList(detail)).isFalse();

  }

  private LocationDetailsForm buildForm() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    form.setApproximateProjectLocationFromShore("approx");
    form.setFacilitiesOffshore(true);
    form.setTransportsMaterialsToShore(true);
    form.setTransportationMethod("method");
    form.setPipelineRouteDetails("Route details");
    form.setRouteSurveyUndertaken(true);
    form.setWithinLimitsOfDeviation(true);
    var localDate = LocalDate.ofInstant(SURVEY_CONCLUDED_DATE, ZoneId.systemDefault());
    form.setSurveyConcludedYear(localDate.getYear());
    form.setSurveyConcludedMonth(localDate.getMonthValue());
    form.setSurveyConcludedDay(localDate.getDayOfMonth());
    return form;
  }

  private PadLocationDetails buildEntity() {
    var entity = new PadLocationDetails();
    entity.setWithinSafetyZone(HseSafetyZone.NO);
    entity.setPwaApplicationDetail(pwaApplicationDetail);
    entity.setApproximateProjectLocationFromShore("approx");
    entity.setFacilitiesOffshore(true);
    entity.setTransportsMaterialsToShore(true);
    entity.setTransportationMethod("method");
    entity.setPipelineRouteDetails("Route details");
    entity.setRouteSurveyUndertaken(true);
    entity.setWithinLimitsOfDeviation(true);
    entity.setSurveyConcludedTimestamp(SURVEY_CONCLUDED_DATE);
    return entity;
  }
}