package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.otherproperties.OtherPropertiesValueView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadPipelineOtherPropertiesRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadPipelineOtherPropertiesService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineOtherPropertiesDataValidator;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineOtherPropertiesValidator;


@RunWith(MockitoJUnitRunner.class)
public class PadPipelineOtherPropertiesServiceTest {

  private PadPipelineOtherPropertiesService padPipelineOtherPropertiesService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository;

  private PipelineOtherPropertiesValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  private OtherPropertiesEntityBuilder entityBuilder = new OtherPropertiesEntityBuilder();
  private OtherPropertiesFormBuilder formBuilder = new OtherPropertiesFormBuilder();


  @Before
  public void setUp() {
    validator = new PipelineOtherPropertiesValidator(new PipelineOtherPropertiesDataValidator(new MinMaxInputValidator()));
    padPipelineOtherPropertiesService = new PadPipelineOtherPropertiesService(padPipelineOtherPropertiesRepository,
        validator, pwaApplicationDetailService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }


  // Entity/Form  Retrieval/Mapping Tests
  @Test
  public void getPipelineOtherPropertiesEntity_existingEntitiesReturned() {
    var expectedEntityList = entityBuilder.createAllEntities(pwaApplicationDetail);
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(expectedEntityList);
    var actualEntityList = padPipelineOtherPropertiesService.getPipelineOtherPropertyEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  public void getPipelineOtherPropertiesEntity_newEntitiesReturned() {
    var expectedEntityList = new ArrayList<>();
    for (OtherPipelineProperty property: OtherPipelineProperty.asList()) {
      var entity = new PadPipelineOtherProperties(pwaApplicationDetail, property);
      expectedEntityList.add(entity);
    }
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var actualEntityList = padPipelineOtherPropertiesService.getPipelineOtherPropertyEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  public void mapEntityToForm() {
    var actualForm = new PipelineOtherPropertiesForm();
    entityBuilder.setPhaseDataOnAppDetail(pwaApplicationDetail);

    padPipelineOtherPropertiesService.mapEntitiesToForm(actualForm, entityBuilder.createAllEntities(pwaApplicationDetail), pwaApplicationDetail);
    assertThat(actualForm).isEqualTo(formBuilder.createFullForm());
  }

  @Test
  public void mapEntityToForm_otherPhaseNotSelected() {
    var actualForm = new PipelineOtherPropertiesForm();
    entityBuilder.setPhaseDataOnAppDetail_otherPhaseExcluded(pwaApplicationDetail);

    var expectedForm = formBuilder.createFullForm();
    expectedForm.getPhasesSelection().remove(PropertyPhase.OTHER);
    expectedForm.setOtherPhaseDescription(null);

    padPipelineOtherPropertiesService.mapEntitiesToForm(actualForm, entityBuilder.createAllEntities(pwaApplicationDetail), pwaApplicationDetail);
    assertThat(actualForm).isEqualTo(expectedForm);
  }

  @Test
  public void saveEntitiesUsingForm() {
    var actualEntities = entityBuilder.createBlankEntities(pwaApplicationDetail);
    padPipelineOtherPropertiesService.saveEntitiesUsingForm(formBuilder.createFullForm(), actualEntities, pwaApplicationDetail);

    assertThat(actualEntities).isEqualTo(entityBuilder.createAllEntities(pwaApplicationDetail));
    verify(pwaApplicationDetailService, times(1)).setPhasesPresent(pwaApplicationDetail,
        entityBuilder.getPhaseDataForAppDetail(), entityBuilder.getOtherPhaseDescription());
    verify(padPipelineOtherPropertiesRepository, times(1)).saveAll(actualEntities);
  }

  @Test
  public void getOtherPropertiesView() {
    var expectedEntityList = entityBuilder.createAllEntities(pwaApplicationDetail);
    entityBuilder.setPhaseDataOnAppDetail(pwaApplicationDetail);
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(expectedEntityList);

    var otherPropertiesView = padPipelineOtherPropertiesService.getOtherPropertiesView(pwaApplicationDetail);

    assertThat(otherPropertiesView.getPropertyValueMap().get(OtherPipelineProperty.WAX_CONTENT))
        .isEqualTo(new OtherPropertiesValueView(PropertyAvailabilityOption.NOT_AVAILABLE, null, null));

    assertThat(otherPropertiesView.getPropertyValueMap().get(OtherPipelineProperty.MERCURY))
        .isEqualTo(new OtherPropertiesValueView(PropertyAvailabilityOption.AVAILABLE, "3", "5"));

    assertThat(otherPropertiesView.getSelectedPropertyPhases())
        .containsAll(entityBuilder.getPhaseDataForAppDetail());

    assertThat(otherPropertiesView.getOtherPhaseDescription())
        .isEqualTo(entityBuilder.getOtherPhaseDescription());

  }


  //Validation / Checking Tests
  @Test
  public void validate_valid() {
    var form = formBuilder.createFullForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padPipelineOtherPropertiesService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  public void validate_invalid() {
    var form = formBuilder.createFullForm();
    form.getPropertyDataFormMap().get(OtherPipelineProperty.ACID_NUM).setPropertyAvailabilityOption(null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padPipelineOtherPropertiesService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertTrue(bindingResult.hasErrors());
  }

  @Test
  public void isComplete_valid() {
    entityBuilder.setPhaseDataOnAppDetail(pwaApplicationDetail);
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        entityBuilder.createAllEntities(pwaApplicationDetail));
    var isValid = padPipelineOtherPropertiesService.isComplete(pwaApplicationDetail);
    assertTrue(isValid);
  }

  @Test
  public void isComplete_invalid() {
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var isValid = padPipelineOtherPropertiesService.isComplete(pwaApplicationDetail);
    assertFalse(isValid);
  }

  @Test
  public void cleanupData_hiddenData() {

    var density = new PadPipelineOtherProperties();
    density.setMinValue(BigDecimal.ONE);
    density.setMaxValue(BigDecimal.TEN);
    density.setAvailabilityOption(PropertyAvailabilityOption.NOT_PRESENT);
    density.setPropertyName(OtherPipelineProperty.DENSITY_GRAVITY);

    var mercury = new PadPipelineOtherProperties();
    mercury.setMinValue(BigDecimal.ONE);
    mercury.setMaxValue(BigDecimal.TEN);
    mercury.setAvailabilityOption(PropertyAvailabilityOption.NOT_AVAILABLE);
    mercury.setPropertyName(OtherPipelineProperty.MERCURY);

    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        List.of(density, mercury));

    padPipelineOtherPropertiesService.cleanupData(pwaApplicationDetail);

    assertThat(density.getMinValue()).isNull();
    assertThat(density.getMaxValue()).isNull();

    assertThat(mercury.getMinValue()).isNull();
    assertThat(mercury.getMaxValue()).isNull();

    verify(padPipelineOtherPropertiesRepository, times(1)).saveAll(eq(List.of(density, mercury)));

  }

  @Test
  public void cleanupData_noHiddenData() {

    var density = new PadPipelineOtherProperties();
    density.setMinValue(BigDecimal.ONE);
    density.setMaxValue(BigDecimal.TEN);
    density.setAvailabilityOption(PropertyAvailabilityOption.AVAILABLE);
    density.setPropertyName(OtherPipelineProperty.DENSITY_GRAVITY);

    var mercury = new PadPipelineOtherProperties();
    mercury.setMinValue(BigDecimal.ONE);
    mercury.setMaxValue(BigDecimal.TEN);
    mercury.setAvailabilityOption(PropertyAvailabilityOption.NOT_AVAILABLE);
    mercury.setPropertyName(OtherPipelineProperty.MERCURY);

    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        List.of(density, mercury));

    padPipelineOtherPropertiesService.cleanupData(pwaApplicationDetail);

    assertThat(density.getMinValue()).isNotNull();
    assertThat(density.getMaxValue()).isNotNull();

    assertThat(mercury.getMinValue()).isNull();
    assertThat(mercury.getMaxValue()).isNull();

    verify(padPipelineOtherPropertiesRepository, times(1)).saveAll(eq(List.of(mercury)));

  }


}