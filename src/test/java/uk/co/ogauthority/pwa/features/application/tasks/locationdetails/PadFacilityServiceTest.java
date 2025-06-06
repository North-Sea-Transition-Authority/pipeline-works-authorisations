package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacility;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacilityService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PadFacilityServiceTest {

  @Mock
  private PadFacilityRepository padFacilityRepository;

  @Mock
  private DevukFacilityService devukFacilityService;

  @Spy
  private SearchSelectorService searchSelectorService;

  private PadFacilityService padFacilityService;
  private PadFacility padFacility;
  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    padFacilityService = new PadFacilityService(padFacilityRepository, devukFacilityService, searchSelectorService);
    padFacility = new PadFacility();
    pwaApplicationDetail = new PwaApplicationDetail();
    when(padFacilityRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(padFacility));
  }

  @Test
  void getFacilities() {
    var result = padFacilityService.getFacilities(pwaApplicationDetail);
    assertThat(result).containsExactly(padFacility);
  }

  @Test
  void setFacilities_NullForm() {
    var form = new LocationDetailsForm();
    padFacilityService.setFacilities(pwaApplicationDetail, form);
    verify(padFacilityRepository, never()).save(any());
  }

  @Test
  void setFacilities_WithinZone_No() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    padFacilityService.setFacilities(pwaApplicationDetail, form);
    verify(padFacilityRepository, never()).save(any());
  }

  @Test
  void setFacilities_WithinZone_Partially() {
    var facility = new DevukFacility();
    var facilityIds = List.of("1");
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    form.getPartiallyWithinSafetyZoneForm().setFacilities(facilityIds);

    when(devukFacilityService.getFacilitiesInIds(any())).thenReturn(List.of(facility));

    padFacilityService.setFacilities(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadFacility.class);
    verify(padFacilityRepository, times(1)).save(captor.capture());
    verify(padFacilityRepository, times(1)).delete(padFacility);

    assertThat(captor.getValue().getFacility()).isEqualTo(facility);
  }

  @Test
  void setFacilities_WithinZone_Yes() {
    var facility = new DevukFacility();
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.getCompletelyWithinSafetyZoneForm().setFacilities(List.of("1"));

    when(devukFacilityService.getFacilitiesInIds(any())).thenReturn(List.of(facility));

    padFacilityService.setFacilities(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadFacility.class);
    verify(padFacilityRepository, times(1)).save(captor.capture());
    verify(padFacilityRepository, times(1)).delete(padFacility);

    assertThat(captor.getValue().getFacility()).isEqualTo(facility);
  }

  @Test
  void mapFacilitiesToView_WithinZone_Null() {
    var devukFacility = new DevukFacility();
    padFacility.setFacility(devukFacility);
    var form = new LocationDetailsForm();
    var modelAndView = new ModelAndView();
    padFacilityService.mapFacilitiesToView(List.of(padFacility), form, modelAndView);
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfPartially")).isNull();
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfYes")).isNull();
  }

  @Test
  void mapFacilitiesToView_WithinZone_No() {
    var devukFacility = new DevukFacility(1, "test");
    padFacility.setFacility(devukFacility);
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    var modelAndView = new ModelAndView();
    padFacilityService.mapFacilitiesToView(List.of(), form, modelAndView);
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfPartially")).isNull();
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfYes")).isNull();
  }

  @Test
  void mapFacilitiesToView_WithinZone_Partially() {
    var devukFacility = new DevukFacility(1, "facility");
    padFacility.setFacility(devukFacility);
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    var modelAndView = new ModelAndView();
    padFacilityService.mapFacilitiesToView(List.of(padFacility), form, modelAndView);
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfPartially")).containsExactly(
        entry("1", "facility")
    );
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfYes")).isNull();
  }

  @Test
  void mapFacilitiesToView_WithinZone_Yes() {
    var devukFacility = new DevukFacility(1, "facility");
    padFacility.setFacility(devukFacility);
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    var modelAndView = new ModelAndView();
    padFacilityService.mapFacilitiesToView(List.of(padFacility), form, modelAndView);
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfPartially")).isNull();
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfYes")).containsExactly(
        entry("1", "facility")
    );
  }

  @Test
  void setFacilities_FreeText_WithinZone_Partially() {
    var facilityIds = List.of(SearchSelectable.FREE_TEXT_PREFIX + "1");
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    form.getPartiallyWithinSafetyZoneForm().setFacilities(facilityIds);

    padFacilityService.setFacilities(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadFacility.class);
    verify(padFacilityRepository, times(1)).save(captor.capture());
    verify(padFacilityRepository, times(1)).delete(padFacility);

    assertThat(captor.getValue().getFacilityNameManualEntry()).isEqualTo("1");
  }

  @Test
  void setFacilities__FreeText_WithinZone_Yes() {
    var facilityIds = List.of(SearchSelectable.FREE_TEXT_PREFIX + "1");
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.getCompletelyWithinSafetyZoneForm().setFacilities(facilityIds);

    padFacilityService.setFacilities(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadFacility.class);
    verify(padFacilityRepository, times(1)).save(captor.capture());
    verify(padFacilityRepository, times(1)).delete(padFacility);

    assertThat(captor.getValue().getFacilityNameManualEntry()).isEqualTo("1");
  }

  @Test
  void mapFacilitiesToView_FreeText_WithinZone_Partially() {
    var devukFacility = new DevukFacility(1, "facility");
    padFacility.setFacilityNameManualEntry("freeText");
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    var modelAndView = new ModelAndView();
    padFacilityService.mapFacilitiesToView(List.of(padFacility), form, modelAndView);
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfPartially")).containsExactly(
        entry(SearchSelectable.FREE_TEXT_PREFIX + padFacility.getFacilityNameManualEntry(),
            padFacility.getFacilityNameManualEntry())
    );
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfYes")).isNull();
  }

  @Test
  void mapFacilitiesToView_FreeText_WithinZone_Yes() {
    var devukFacility = new DevukFacility(1, "facility");
    padFacility.setFacilityNameManualEntry("freeText");
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    var modelAndView = new ModelAndView();
    padFacilityService.mapFacilitiesToView(List.of(padFacility), form, modelAndView);
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfPartially")).isNull();
    assertThat((Map<String, String>) modelAndView.getModel().get("preselectedFacilitiesIfYes")).containsExactly(
        entry(SearchSelectable.FREE_TEXT_PREFIX + padFacility.getFacilityNameManualEntry(),
            padFacility.getFacilityNameManualEntry())
    );
  }
}