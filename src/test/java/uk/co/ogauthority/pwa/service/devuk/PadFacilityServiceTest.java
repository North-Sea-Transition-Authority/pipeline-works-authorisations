package uk.co.ogauthority.pwa.service.devuk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.LocationDetailsForm;
import uk.co.ogauthority.pwa.repository.devuk.PadFacilityRepository;

@RunWith(MockitoJUnitRunner.class)
public class PadFacilityServiceTest {

  @Mock
  private PadFacilityRepository padFacilityRepository;

  private PadFacilityService padFacilityService;
  private PadFacility padFacility;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    padFacilityService = new PadFacilityService(padFacilityRepository);
    padFacility = new PadFacility();
    pwaApplicationDetail = new PwaApplicationDetail();
    when(padFacilityRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(padFacility));
  }

  @Test
  public void getFacilities() {
    var result = padFacilityService.getFacilities(pwaApplicationDetail);
    assertThat(result).containsExactly(padFacility);
  }

  @Test
  public void setFacilities_NullForm() {
    var form = new LocationDetailsForm();
    padFacilityService.setFacilities(pwaApplicationDetail, form);
    verify(padFacilityRepository, never()).save(any());
  }

  @Test
  public void setFacilities_WithinZone_No() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    padFacilityService.setFacilities(pwaApplicationDetail, form);
    verify(padFacilityRepository, never()).save(any());
  }

  @Test
  public void setFacilities_WithinZone_Partially() {
    var facility = new DevukFacility();
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    form.setFacilitiesIfPartially(List.of(facility));
    padFacilityService.setFacilities(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadFacility.class);
    verify(padFacilityRepository, times(1)).save(captor.capture());
    verify(padFacilityRepository, times(1)).delete(padFacility);

    assertThat(captor.getValue().getFacility()).isEqualTo(facility);
  }

  @Test
  public void setFacilities_WithinZone_Yes() {
    var facility = new DevukFacility();
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    form.setFacilitiesIfYes(List.of(facility));
    padFacilityService.setFacilities(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadFacility.class);
    verify(padFacilityRepository, times(1)).save(captor.capture());
    verify(padFacilityRepository, times(1)).delete(padFacility);

    assertThat(captor.getValue().getFacility()).isEqualTo(facility);
  }

  @Test
  public void mapFacilitiesToForm_WithinZone_Null() {
    var devukFacility = new DevukFacility();
    padFacility.setFacility(devukFacility);
    var form = new LocationDetailsForm();
    padFacilityService.mapFacilitiesToForm(List.of(padFacility), form);
    assertThat(form.getFacilitiesIfPartially()).isEmpty();
    assertThat(form.getFacilitiesIfYes()).isEmpty();
  }

  @Test
  public void mapFacilitiesToForm_WithinZone_No() {
    var devukFacility = new DevukFacility();
    padFacility.setFacility(devukFacility);
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    padFacilityService.mapFacilitiesToForm(List.of(padFacility), form);
    assertThat(form.getFacilitiesIfPartially()).isEmpty();
    assertThat(form.getFacilitiesIfYes()).isEmpty();
  }

  @Test
  public void mapFacilitiesToForm_WithinZone_Partially() {
    var devukFacility = new DevukFacility();
    padFacility.setFacility(devukFacility);
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.PARTIALLY);
    padFacilityService.mapFacilitiesToForm(List.of(padFacility), form);
    assertThat(form.getFacilitiesIfPartially()).containsExactly(devukFacility);
    assertThat(form.getFacilitiesIfYes()).isEmpty();
  }

  @Test
  public void mapFacilitiesToForm_WithinZone_Yes() {
    var devukFacility = new DevukFacility();
    padFacility.setFacility(devukFacility);
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.YES);
    padFacilityService.mapFacilitiesToForm(List.of(padFacility), form);
    assertThat(form.getFacilitiesIfPartially()).isEmpty();
    assertThat(form.getFacilitiesIfYes()).containsExactly(devukFacility);
  }
}