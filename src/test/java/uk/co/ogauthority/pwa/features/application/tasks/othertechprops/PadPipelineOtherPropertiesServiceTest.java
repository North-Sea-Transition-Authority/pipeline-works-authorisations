package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;


@ExtendWith(MockitoExtension.class)
class PadPipelineOtherPropertiesServiceTest {

  private PadPipelineOtherPropertiesService padPipelineOtherPropertiesService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository;

  @Mock
  private EntityCopyingService entityCopyingService;

  private PipelineOtherPropertiesValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  private OtherPropertiesFormBuilder formBuilder = new OtherPropertiesFormBuilder();


  @BeforeEach
  void setUp() {
    validator = new PipelineOtherPropertiesValidator(new PipelineOtherPropertiesDataValidator(new MinMaxInputValidator()));

    padPipelineOtherPropertiesService = new PadPipelineOtherPropertiesService(
        padPipelineOtherPropertiesRepository,
        validator,
        pwaApplicationDetailService,
        entityCopyingService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }


  // Entity/Form  Retrieval/Mapping Tests
  @Test
  void getPipelineOtherPropertiesEntity_existingEntitiesReturned() {
    var expectedEntityList = PadPipelineOtherPropertiesTestUtil.createAllEntities(pwaApplicationDetail);
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(expectedEntityList);
    var actualEntityList = padPipelineOtherPropertiesService.getPipelineOtherPropertyEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  void getPipelineOtherPropertiesEntity_Petroleum_newEntitiesReturned() {
    var expectedEntityList = new ArrayList<>();
    for (OtherPipelineProperty property: OtherPipelineProperty.asList(PwaResourceType.PETROLEUM)) {
      var entity = new PadPipelineOtherProperties(pwaApplicationDetail, property);
      expectedEntityList.add(entity);
    }
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var actualEntityList = padPipelineOtherPropertiesService.getPipelineOtherPropertyEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  void getPipelineOtherPropertiesEntity_CCUS_newEntitiesReturned() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.CCUS);
    pwaApplicationDetail.setPwaApplication(application);

    var expectedEntityList = new ArrayList<>();
    for (OtherPipelineProperty property: OtherPipelineProperty.asList(PwaResourceType.CCUS)) {
      var entity = new PadPipelineOtherProperties(pwaApplicationDetail, property);
      expectedEntityList.add(entity);
    }
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var actualEntityList = padPipelineOtherPropertiesService.getPipelineOtherPropertyEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  void mapEntityToForm() {
    var actualForm = new PipelineOtherPropertiesForm();
    PadPipelineOtherPropertiesTestUtil.setPhaseDataOnAppDetail(pwaApplicationDetail);

    padPipelineOtherPropertiesService.mapEntitiesToForm(actualForm, PadPipelineOtherPropertiesTestUtil.createAllEntities(pwaApplicationDetail), pwaApplicationDetail);
    assertThat(actualForm).isEqualTo(formBuilder.createFullForm());
  }

  @Test
  void mapEntityToForm_otherPhaseNotSelected() {
    var actualForm = new PipelineOtherPropertiesForm();
    PadPipelineOtherPropertiesTestUtil.setPhaseDataOnAppDetail_otherPhaseExcluded(pwaApplicationDetail);

    var expectedForm = formBuilder.createFullForm();
    expectedForm.getPhasesSelection().remove(PropertyPhase.OTHER);
    expectedForm.setOtherPhaseDescription(null);

    padPipelineOtherPropertiesService.mapEntitiesToForm(actualForm, PadPipelineOtherPropertiesTestUtil.createAllEntities(pwaApplicationDetail), pwaApplicationDetail);
    assertThat(actualForm).isEqualTo(expectedForm);
  }

  @Test
  void saveEntitiesUsingForm() {
    var actualEntities = PadPipelineOtherPropertiesTestUtil.createBlankEntities(pwaApplicationDetail);
    padPipelineOtherPropertiesService.saveEntitiesUsingForm(formBuilder.createFullForm(), actualEntities, pwaApplicationDetail);

    verify(padPipelineOtherPropertiesRepository, times(1)).saveAll(actualEntities);
    verify(pwaApplicationDetailService, times(1)).setPhasesPresent(pwaApplicationDetail,
        PadPipelineOtherPropertiesTestUtil.getPhaseDataForAppDetail(), PadPipelineOtherPropertiesTestUtil.getOtherPhaseDescription());
  }

  @Test
  void getOtherPropertiesView() {
    var expectedEntityList = PadPipelineOtherPropertiesTestUtil.createAllEntities(pwaApplicationDetail);
    PadPipelineOtherPropertiesTestUtil.setPhaseDataOnAppDetail(pwaApplicationDetail);
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(expectedEntityList);

    var otherPropertiesView = padPipelineOtherPropertiesService.getOtherPropertiesView(pwaApplicationDetail);

    assertThat(otherPropertiesView.getPropertyValueMap().get(OtherPipelineProperty.WAX_CONTENT))
        .isEqualTo(new OtherPropertiesValueView(PropertyAvailabilityOption.NOT_AVAILABLE, null, null));

    assertThat(otherPropertiesView.getPropertyValueMap().get(OtherPipelineProperty.MERCURY))
        .isEqualTo(new OtherPropertiesValueView(PropertyAvailabilityOption.AVAILABLE, "3", "5"));

    assertThat(otherPropertiesView.getSelectedPropertyPhases())
        .containsAll(PadPipelineOtherPropertiesTestUtil.getPhaseDataForAppDetail());

    assertThat(otherPropertiesView.getOtherPhaseDescription())
        .isEqualTo(PadPipelineOtherPropertiesTestUtil.getOtherPhaseDescription());

  }


  //Validation / Checking Tests
  @Test
  void validate_valid() {
    var form = formBuilder.createFullForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padPipelineOtherPropertiesService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_invalid() {
    var form = formBuilder.createFullForm();
    form.getPropertyDataFormMap().get(OtherPipelineProperty.ACID_NUM).setPropertyAvailabilityOption(null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padPipelineOtherPropertiesService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertTrue(bindingResult.hasErrors());
  }

  @Test
  void isComplete_valid() {
    PadPipelineOtherPropertiesTestUtil.setPhaseDataOnAppDetail(pwaApplicationDetail);
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        PadPipelineOtherPropertiesTestUtil.createAllEntities(pwaApplicationDetail));
    var isValid = padPipelineOtherPropertiesService.isComplete(pwaApplicationDetail);
    assertTrue(isValid);
  }

  @Test
  void isComplete_invalid() {
    when(padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var isValid = padPipelineOtherPropertiesService.isComplete(pwaApplicationDetail);
    assertFalse(isValid);
  }

  @Test
  void cleanupData_hiddenData() {

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

    verify(padPipelineOtherPropertiesRepository, times(1)).saveAll(List.of(density, mercury));

  }

  @Test
  void cleanupData_noHiddenData() {

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

    verify(padPipelineOtherPropertiesRepository, times(1)).saveAll(List.of(mercury));

  }


}
