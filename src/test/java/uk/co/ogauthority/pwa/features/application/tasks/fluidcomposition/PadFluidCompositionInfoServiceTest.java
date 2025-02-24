package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;


@ExtendWith(MockitoExtension.class)
class PadFluidCompositionInfoServiceTest {

  private PadFluidCompositionInfoService padFluidCompositionInfoService;

  @Mock
  private PadFluidCompositionInfoRepository padFluidCompositionInfoRepository;

  @Mock
  private EntityCopyingService entityCopyingService;

  private FluidCompositionValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    validator = new FluidCompositionValidator(new FluidCompositionDataValidator(new DecimalInputValidator()), new FluidCompositionFormValidator());
    padFluidCompositionInfoService = new PadFluidCompositionInfoService(
        padFluidCompositionInfoRepository,
        validator,
        entityCopyingService
    );
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }

  private FluidCompositionForm createValidForm() {
    var h20Form = new FluidCompositionDataForm();
    h20Form.setChemicalMeasurementType(ChemicalMeasurementType.NONE);
    var n2Form = new FluidCompositionDataForm();
    n2Form.setChemicalMeasurementType(ChemicalMeasurementType.TRACE);
    var c1Form = new FluidCompositionDataForm();
    c1Form.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    c1Form.setMeasurementValue(new DecimalInput(BigDecimal.valueOf(100)));

    var form = new FluidCompositionForm();
    Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap = new HashMap<>();
    chemicalDataFormMap.put(Chemical.H2O, h20Form);
    chemicalDataFormMap.put(Chemical.N2, n2Form);
    chemicalDataFormMap.put(Chemical.C1, c1Form);
    form.setChemicalDataFormMap(chemicalDataFormMap);

    return form;
  }

  private PadFluidCompositionInfo createValidEntity(Chemical chemical, ChemicalMeasurementType chemicalMeasurementType) {
    var entity = new PadFluidCompositionInfo();
    entity.setPwaApplicationDetail(pwaApplicationDetail);
    entity.setChemicalName(chemical);
    entity.setChemicalMeasurementType(chemicalMeasurementType);
    return entity;
  }

  private PadFluidCompositionInfo createValidEntityWithMoleValue(Chemical chemical, BigDecimal moleValue) {
    var entity = new PadFluidCompositionInfo();
    entity.setPwaApplicationDetail(pwaApplicationDetail);
    entity.setChemicalName(chemical);
    entity.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    entity.setMoleValue(moleValue);
    return entity;
  }

  // Entity/Form  Retrieval/Mapping Tests
  @Test
  void getPadFluidCompositionInfoEntities_existingEntitiesReturned() {
    var expectedEntityList = List.of(
        createValidEntity(Chemical.H2O, ChemicalMeasurementType.NONE),
        createValidEntity(Chemical.AR, ChemicalMeasurementType.PPMV_100K),
        createValidEntity(Chemical.HYDROCARBONS, ChemicalMeasurementType.TRACE));
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(expectedEntityList);
    var actualEntityList = padFluidCompositionInfoService.getPadFluidCompositionInfoEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  void getPadFluidCompositionInfoEntities_newPetroleumEntitiesReturned() {
    var expectedEntityList = new ArrayList<>();
    for (Chemical chemical: Chemical.getAllByResourceType(PwaResourceType.PETROLEUM)) {
      var padFluidCompositionInfo = new PadFluidCompositionInfo(pwaApplicationDetail, chemical);
      expectedEntityList.add(padFluidCompositionInfo);
    }
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var actualEntityList = padFluidCompositionInfoService.getPadFluidCompositionInfoEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  void getPadFluidCompositionInfoEntities_newHydrogenEntitiesReturned() {
    var application = pwaApplicationDetail.getPwaApplication();
    application.setResourceType(PwaResourceType.HYDROGEN);
    pwaApplicationDetail.setPwaApplication(application);

    var expectedEntityList = new ArrayList<>();
    for (Chemical chemical: Chemical.getAllByResourceType(PwaResourceType.HYDROGEN)) {
      var padFluidCompositionInfo = new PadFluidCompositionInfo(pwaApplicationDetail, chemical);
      expectedEntityList.add(padFluidCompositionInfo);
    }
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var actualEntityList = padFluidCompositionInfoService.getPadFluidCompositionInfoEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  void getPadFluidCompositionInfoEntities_newCCUSEntitiesReturned() {
    var application = pwaApplicationDetail.getPwaApplication();
    application.setResourceType(PwaResourceType.CCUS);
    pwaApplicationDetail.setPwaApplication(application);

    var expectedEntityList = new ArrayList<>();
    for (Chemical chemical: Chemical.getAllByResourceType(PwaResourceType.CCUS)) {
      var padFluidCompositionInfo = new PadFluidCompositionInfo(pwaApplicationDetail, chemical);
      expectedEntityList.add(padFluidCompositionInfo);
    }
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var actualEntityList = padFluidCompositionInfoService.getPadFluidCompositionInfoEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  void mapEntityToForm() {
    var form = new FluidCompositionForm();
    padFluidCompositionInfoService.mapEntitiesToForm(form, List.of(createValidEntity(Chemical.H2O, ChemicalMeasurementType.NONE),
        createValidEntity(Chemical.N2, ChemicalMeasurementType.TRACE), createValidEntityWithMoleValue(Chemical.C1,
            BigDecimal.valueOf(100))));

    assertThat(form).isEqualTo(createValidForm());
  }

  @Test
  void getFluidCompositionView() {

    var higherAmountFluidComp = createValidEntity(Chemical.C2, ChemicalMeasurementType.MOLE_PERCENTAGE);
    higherAmountFluidComp.setMoleValue(BigDecimal.valueOf(0.1));

    var entities = List.of(createValidEntity(Chemical.H2O, ChemicalMeasurementType.NONE),
        createValidEntity(Chemical.C1, ChemicalMeasurementType.TRACE),
        createValidEntity(Chemical.HG, ChemicalMeasurementType.PPMV_100K),
        higherAmountFluidComp);
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(entities);

    var fluidCompositionView = padFluidCompositionInfoService.getFluidCompositionView(pwaApplicationDetail);
    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.H2O).getChemicalMeasurementType()).isEqualTo(ChemicalMeasurementType.NONE);
    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.C1).getChemicalMeasurementType()).isEqualTo(ChemicalMeasurementType.TRACE);
    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.C2).getChemicalMeasurementType()).isEqualTo(ChemicalMeasurementType.MOLE_PERCENTAGE);
    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.HG).getChemicalMeasurementType()).isEqualTo(ChemicalMeasurementType.PPMV_100K);
    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.C2).getMeasurementValue().createBigDecimalOrNull()).isEqualTo(BigDecimal.valueOf(0.1));

    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.N2)).isNull();
  }

  @Test
  void saveEntitiesUsingForm() {
    var form = new FluidCompositionForm();
    var h20Form = new FluidCompositionDataForm();
    h20Form.setChemicalMeasurementType(ChemicalMeasurementType.NONE);
    var c1Form = new FluidCompositionDataForm();
    c1Form.setChemicalMeasurementType(ChemicalMeasurementType.TRACE);
    var c2Form = new FluidCompositionDataForm();
    c2Form.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    c2Form.setMeasurementValue(new DecimalInput(BigDecimal.valueOf(100)));
    var hgForm = new FluidCompositionDataForm();
    hgForm.setChemicalMeasurementType(ChemicalMeasurementType.PPMV_100K);
    hgForm.setMeasurementValue(new DecimalInput(BigDecimal.valueOf(100)));

    Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap = new HashMap<>();
    chemicalDataFormMap.put(Chemical.H2O, h20Form);
    chemicalDataFormMap.put(Chemical.C1, c1Form);
    chemicalDataFormMap.put(Chemical.C2, c2Form);
    chemicalDataFormMap.put(Chemical.HG, hgForm);
    form.setChemicalDataFormMap(chemicalDataFormMap);

    var entities = List.of(
        createValidEntity(Chemical.H2O, ChemicalMeasurementType.NONE),
        createValidEntity(Chemical.C1, ChemicalMeasurementType.TRACE),
        createValidEntity(Chemical.C2, ChemicalMeasurementType.MOLE_PERCENTAGE),
        createValidEntity(Chemical.HG, ChemicalMeasurementType.PPMV_100K));

    padFluidCompositionInfoService.saveEntitiesUsingForm(form, entities);

    ArgumentCaptor<List<PadFluidCompositionInfo>> captor = ArgumentCaptor.forClass(List.class);
    verify(padFluidCompositionInfoRepository).saveAll(captor.capture());
    assertThat(captor.getValue())
        .extracting(
            PadFluidCompositionInfo::getChemicalName,
            PadFluidCompositionInfo::getChemicalMeasurementType,
            PadFluidCompositionInfo::getMoleValue
        )
        .containsExactly(
            tuple(Chemical.H2O, ChemicalMeasurementType.NONE, null),
            tuple(Chemical.C1, ChemicalMeasurementType.TRACE, null),
            tuple(Chemical.C2, ChemicalMeasurementType.MOLE_PERCENTAGE, BigDecimal.valueOf(100)),
            tuple(Chemical.HG, ChemicalMeasurementType.PPMV_100K, BigDecimal.valueOf(100))
        );
  }

  //Validation / Checking Tests
  @Test
  void validate_valid() {
    var form = createValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padFluidCompositionInfoService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_invalid() {
    var form = createValidForm();
    form.getChemicalDataFormMap().get(Chemical.H2O).setChemicalMeasurementType(null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padFluidCompositionInfoService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertTrue(bindingResult.hasErrors());
  }

  @Test
  void isComplete_valid() {
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(createValidEntityWithMoleValue(Chemical.H2O, BigDecimal.valueOf(100))));
    var isValid = padFluidCompositionInfoService.isComplete(pwaApplicationDetail);
    assertTrue(isValid);
  }

  @Test
  void isComplete_invalid() {
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var isValid = padFluidCompositionInfoService.isComplete(pwaApplicationDetail);
    assertFalse(isValid);
  }

  @Test
  void cleanupData_hiddenData() {

    var co2 = new PadFluidCompositionInfo();
    co2.setMoleValue(BigDecimal.TEN);
    co2.setChemicalMeasurementType(ChemicalMeasurementType.TRACE);
    co2.setChemicalName(Chemical.CO2);

    var h2o = new PadFluidCompositionInfo();
    h2o.setMoleValue(BigDecimal.ONE);
    h2o.setChemicalMeasurementType(ChemicalMeasurementType.NONE);
    h2o.setChemicalName(Chemical.H2O);

    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(co2, h2o));

    padFluidCompositionInfoService.cleanupData(pwaApplicationDetail);

    assertThat(co2.getMoleValue()).isNull();

    assertThat(h2o.getMoleValue()).isNull();

    verify(padFluidCompositionInfoRepository, times(1)).saveAll(List.of(co2, h2o));

  }

  @Test
  void cleanupData_noHiddenData() {

    var co2 = new PadFluidCompositionInfo();
    co2.setMoleValue(BigDecimal.TEN);
    co2.setChemicalMeasurementType(ChemicalMeasurementType.MOLE_PERCENTAGE);
    co2.setChemicalName(Chemical.CO2);

    var h2o = new PadFluidCompositionInfo();
    h2o.setMoleValue(BigDecimal.ONE);
    h2o.setChemicalMeasurementType(ChemicalMeasurementType.NONE);
    h2o.setChemicalName(Chemical.H2O);

    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(co2, h2o));

    padFluidCompositionInfoService.cleanupData(pwaApplicationDetail);

    assertThat(co2.getMoleValue()).isNotNull();

    assertThat(h2o.getMoleValue()).isNull();

    verify(padFluidCompositionInfoRepository, times(1)).saveAll(List.of(h2o));

  }

}
