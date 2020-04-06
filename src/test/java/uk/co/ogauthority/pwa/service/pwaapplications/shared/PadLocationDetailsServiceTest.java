package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import javax.validation.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.LocationDetailsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadLocationDetailsRepository;
import uk.co.ogauthority.pwa.service.devuk.PadFacilityService;
import uk.co.ogauthority.pwa.validators.LocationDetailsValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadLocationDetailsServiceTest {

  @Mock
  private PadLocationDetailsRepository padLocationDetailsRepository;

  @Mock
  private PadFacilityService facilityService;

  @Mock
  private LocationDetailsValidator validator;

  private SpringValidatorAdapter groupValidator;

  private PadLocationDetailsService padLocationDetailsService;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadLocationDetails padLocationDetails;

  @Before
  public void setUp() {
    groupValidator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());
    padLocationDetailsService = new PadLocationDetailsService(padLocationDetailsRepository, facilityService, validator, groupValidator);
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
  }

  private LocationDetailsForm buildForm() {
    var form = new LocationDetailsForm();
    form.setWithinSafetyZone(HseSafetyZone.NO);
    form.setApproximateProjectLocationFromShore("approx");
    form.setFacilitiesOffshore(true);
    form.setTransportsMaterialsToShore(true);
    form.setTransportationMethod("method");
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
    return entity;
  }
}