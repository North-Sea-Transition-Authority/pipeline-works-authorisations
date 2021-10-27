package uk.co.ogauthority.pwa.features.application.tasks.designopconditions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.MinMaxView;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;


@RunWith(MockitoJUnitRunner.class)
public class PadDesignOpConditionsServiceTest {

  private PadDesignOpConditionsService padDesignOpConditionsService;

  private PadDesignOpConditionsMappingService padDesignOpConditionsMappingService;

  @Mock
  private PadDesignOpConditionsRepository padDesignOpConditionsRepository;

  private PadDesignOpConditionsValidator validator;
  
  @Mock
  private EntityCopyingService entityCopyingService;

  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {
    validator = new PadDesignOpConditionsValidator(new MinMaxInputValidator());
    padDesignOpConditionsMappingService = new PadDesignOpConditionsMappingService();
    padDesignOpConditionsService = new PadDesignOpConditionsService(
        padDesignOpConditionsMappingService,
        padDesignOpConditionsRepository,
        validator,
        entityCopyingService
    );
    pwaApplicationDetail = new PwaApplicationDetail();

  }

  // Entity/Form  Retrieval/Mapping tests
  @Test
  public void getDesignOpConditionsEntity_existingNotFound() {
    var actualDesignOpConditions = padDesignOpConditionsService.getDesignOpConditionsEntity(pwaApplicationDetail);
    var expectedDesignOpConditions = new PadDesignOpConditions(pwaApplicationDetail);
    assertThat(actualDesignOpConditions).isEqualTo(expectedDesignOpConditions);
  }

  @Test
  public void mapEntityToForm_full() {
    var actualForm = new DesignOpConditionsForm();
    var entity = PadDesignOpConditionsTestUtil.createValidEntity();
    padDesignOpConditionsService.mapEntityToForm(actualForm, entity);
    assertThat(actualForm).isEqualTo(PadDesignOpConditionsTestUtil.createValidForm());
  }

  @Test
  public void mapFormToEntity_full() {
    var actualEntity = new PadDesignOpConditions();
    var form = PadDesignOpConditionsTestUtil.createValidForm();
    padDesignOpConditionsService.saveEntityUsingForm(form, actualEntity);
    assertThat(actualEntity).isEqualTo(PadDesignOpConditionsTestUtil.createValidEntity());
    verify(padDesignOpConditionsRepository, times(1)).save(any(PadDesignOpConditions.class));
  }

  @Test
  public void getDesignOpConditionsView() {
    var entity = PadDesignOpConditionsTestUtil.createValidEntity();
    when(padDesignOpConditionsRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(entity));
    var actualView = padDesignOpConditionsService.getDesignOpConditionsView(pwaApplicationDetail);

    assertThat(actualView.getTemperatureOpMinMaxView()).isEqualTo(MinMaxView.createMinMaxView("1", "2", UnitMeasurement.DEGREES_CELSIUS));
    assertThat(actualView.getTemperatureDesignMinMaxView()).isEqualTo(MinMaxView.createMinMaxView("3", "4", UnitMeasurement.DEGREES_CELSIUS));
    assertThat(actualView.getPressureOpMinMaxView()).isEqualTo(MinMaxView.createMinMaxView("5", "6", UnitMeasurement.BAR_G));
    assertThat(actualView.getPressureDesignMax()).isEqualTo("7");
    assertThat(actualView.getFlowrateOpMinMaxView()).isEqualTo(MinMaxView.createMinMaxView("9", "10", UnitMeasurement.KSCM_D));
    assertThat(actualView.getFlowrateDesignMinMaxView()).isEqualTo(MinMaxView.createMinMaxView("11", "12", UnitMeasurement.KSCM_D));
    assertThat(actualView.getUvalueDesign()).isEqualTo("14");
  }


  // Validation / Checking tests
  @Test
  public void isComplete_valid() {
    when(padDesignOpConditionsRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(
        PadDesignOpConditionsTestUtil.createValidEntity()));
    var isValid = padDesignOpConditionsService.isComplete(pwaApplicationDetail);
    assertTrue(isValid);
  }

  @Test
  public void isComplete_invalid() {
    when(padDesignOpConditionsRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());
    var isValid = padDesignOpConditionsService.isComplete(pwaApplicationDetail);
    assertFalse(isValid);
  }

  @Test
  public void validate_fullValidation_valid() {
    var bindingResult = new BeanPropertyBindingResult(null, "empty");
    padDesignOpConditionsService.validate(PadDesignOpConditionsTestUtil.createValidForm(), bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  public void validate_invalid() {
    var form = PadDesignOpConditionsTestUtil.createBlankForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padDesignOpConditionsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    Assertions.assertTrue(bindingResult.hasErrors());
  }






}