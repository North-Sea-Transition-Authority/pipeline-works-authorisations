package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.FluidCompositionOption;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadFluidCompositionInfo;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadFluidCompositionInfoRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadFluidCompositionInfoService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.FluidCompositionDataValidator;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.FluidCompositionValidator;


@RunWith(MockitoJUnitRunner.class)
public class PadFluidCompositionInfoServiceTest {

  private PadFluidCompositionInfoService padFluidCompositionInfoService;

  @Mock
  private PadFluidCompositionInfoRepository padFluidCompositionInfoRepository;

  @Mock
  private EntityCopyingService entityCopyingService;

  private FluidCompositionValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    validator = new FluidCompositionValidator(new FluidCompositionDataValidator());
    padFluidCompositionInfoService = new PadFluidCompositionInfoService(
        padFluidCompositionInfoRepository,
        validator,
        entityCopyingService
    );
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }

  private FluidCompositionForm createValidForm() {
    var h20Form = new FluidCompositionDataForm();
    h20Form.setFluidCompositionOption(FluidCompositionOption.NONE);
    var n2Form = new FluidCompositionDataForm();
    n2Form.setFluidCompositionOption(FluidCompositionOption.TRACE);

    var form = new FluidCompositionForm();
    Map<Chemical, FluidCompositionDataForm> chemicalDataFormMap = new HashMap<>();
    chemicalDataFormMap.put(Chemical.H2O, h20Form);
    chemicalDataFormMap.put(Chemical.N2, n2Form);
    form.setChemicalDataFormMap(chemicalDataFormMap);

    return form;
  }

  private PadFluidCompositionInfo createValidEntity(Chemical chemical, FluidCompositionOption fluidCompositionOption) {
    var entity = new PadFluidCompositionInfo();
    entity.setPwaApplicationDetail(pwaApplicationDetail);
    entity.setChemicalName(chemical);
    entity.setFluidCompositionOption(fluidCompositionOption);
    return entity;
  }


  // Entity/Form  Retrieval/Mapping Tests
  @Test
  public void getPadFluidCompositionInfoEntities_existingEntitiesReturned() {
    var expectedEntityList = List.of(createValidEntity(Chemical.H2O, FluidCompositionOption.NONE));
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(expectedEntityList);
    var actualEntityList = padFluidCompositionInfoService.getPadFluidCompositionInfoEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  public void getPadFluidCompositionInfoEntities_newEntitiesReturned() {
    var expectedEntityList = new ArrayList<>();
    for (Chemical chemical: Chemical.asList()) {
      var padFluidCompositionInfo = new PadFluidCompositionInfo(pwaApplicationDetail, chemical);
      expectedEntityList.add(padFluidCompositionInfo);
    }
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var actualEntityList = padFluidCompositionInfoService.getPadFluidCompositionInfoEntities(pwaApplicationDetail);
    assertThat(actualEntityList).isEqualTo(expectedEntityList);
  }

  @Test
  public void mapEntityToForm() {
    var form = new FluidCompositionForm();
    padFluidCompositionInfoService.mapEntitiesToForm(form, List.of(createValidEntity(Chemical.H2O, FluidCompositionOption.NONE),
        createValidEntity(Chemical.N2, FluidCompositionOption.TRACE)));

    assertThat(form).isEqualTo(createValidForm());
  }

  @Test
  public void mapEntityToForm_usingMolePercentage() {
    var form = new FluidCompositionForm();
    var n2Entity= createValidEntity(Chemical.N2, FluidCompositionOption.HIGHER_AMOUNT);
    n2Entity.setMoleValue(BigDecimal.valueOf(1.2));

    padFluidCompositionInfoService.mapEntitiesToForm(form, List.of(createValidEntity(Chemical.H2O, FluidCompositionOption.NONE), n2Entity));
    var expectedForm = createValidForm();
    expectedForm.getChemicalDataFormMap().get(Chemical.N2).setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    expectedForm.getChemicalDataFormMap().get(Chemical.N2).setMoleValue(BigDecimal.valueOf(1.2));

    assertThat(form).isEqualTo(expectedForm);
  }

  @Test
  public void getFluidCompositionView() {

    var higherAmountFluidComp = createValidEntity(Chemical.C2, FluidCompositionOption.HIGHER_AMOUNT);
    higherAmountFluidComp.setMoleValue(BigDecimal.valueOf(0.1));

    var entities = List.of(createValidEntity(Chemical.H2O, FluidCompositionOption.NONE),
        createValidEntity(Chemical.C1, FluidCompositionOption.TRACE),
        higherAmountFluidComp);
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(entities);

    var fluidCompositionView = padFluidCompositionInfoService.getFluidCompositionView(pwaApplicationDetail);

    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.H2O).getFluidCompositionOption())
        .isEqualTo(FluidCompositionOption.NONE);

    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.C1).getFluidCompositionOption())
        .isEqualTo(FluidCompositionOption.TRACE);

    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.C2).getFluidCompositionOption())
        .isEqualTo(FluidCompositionOption.HIGHER_AMOUNT);
    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.C2).getMoleValue())
        .isEqualTo(BigDecimal.valueOf(0.1));

    assertThat(fluidCompositionView.getChemicalDataFormMap().get(Chemical.N2)).isNull();
  }



  //Validation / Checking Tests
  @Test
  public void validate_valid() {
    var form = createValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padFluidCompositionInfoService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  public void validate_invalid() {
    var form = createValidForm();
    form.getChemicalDataFormMap().get(Chemical.H2O).setFluidCompositionOption(null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padFluidCompositionInfoService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertTrue(bindingResult.hasErrors());
  }

  @Test
  public void isComplete_valid() {
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(createValidEntity(Chemical.H2O, FluidCompositionOption.NONE)));
    var isValid = padFluidCompositionInfoService.isComplete(pwaApplicationDetail);
    assertTrue(isValid);
  }

  @Test
  public void isComplete_invalid() {
    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    var isValid = padFluidCompositionInfoService.isComplete(pwaApplicationDetail);
    assertFalse(isValid);
  }

  @Test
  public void cleanupData_hiddenData() {

    var co2 = new PadFluidCompositionInfo();
    co2.setMoleValue(BigDecimal.TEN);
    co2.setFluidCompositionOption(FluidCompositionOption.TRACE);
    co2.setChemicalName(Chemical.CO2);

    var h2o = new PadFluidCompositionInfo();
    h2o.setMoleValue(BigDecimal.ONE);
    h2o.setFluidCompositionOption(FluidCompositionOption.NONE);
    h2o.setChemicalName(Chemical.H2O);

    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(co2, h2o));

    padFluidCompositionInfoService.cleanupData(pwaApplicationDetail);

    assertThat(co2.getMoleValue()).isNull();

    assertThat(h2o.getMoleValue()).isNull();

    verify(padFluidCompositionInfoRepository, times(1)).saveAll(eq(List.of(co2, h2o)));

  }

  @Test
  public void cleanupData_noHiddenData() {

    var co2 = new PadFluidCompositionInfo();
    co2.setMoleValue(BigDecimal.TEN);
    co2.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    co2.setChemicalName(Chemical.CO2);

    var h2o = new PadFluidCompositionInfo();
    h2o.setMoleValue(BigDecimal.ONE);
    h2o.setFluidCompositionOption(FluidCompositionOption.NONE);
    h2o.setChemicalName(Chemical.H2O);

    when(padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(co2, h2o));

    padFluidCompositionInfoService.cleanupData(pwaApplicationDetail);

    assertThat(co2.getMoleValue()).isNotNull();

    assertThat(h2o.getMoleValue()).isNull();

    verify(padFluidCompositionInfoRepository, times(1)).saveAll(eq(List.of(h2o)));

  }

}