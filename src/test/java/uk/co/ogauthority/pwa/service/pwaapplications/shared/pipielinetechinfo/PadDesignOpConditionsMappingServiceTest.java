package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadDesignOpConditionsMappingService;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;


@RunWith(MockitoJUnitRunner.class)
public class PadDesignOpConditionsMappingServiceTest {

  private PadDesignOpConditionsMappingService designOpConditionsMappingService;

  @Before
  public void setUp() {
    designOpConditionsMappingService = new PadDesignOpConditionsMappingService();
  }

  private DesignOpConditionsForm createForm() {
    var form = new DesignOpConditionsForm();
    form.setTemperatureOpMinMax(new MinMaxInput("1", "2"));
    form.setTemperatureDesignMinMax(new MinMaxInput("3", "4"));
    form.setPressureOpInternalExternal(new MinMaxInput("5", "6"));
    form.setPressureDesignInternalExternal(new MinMaxInput("7", "8"));
    form.setFlowrateOpMinMax(new MinMaxInput("9", "10"));
    form.setFlowrateDesignMinMax(new MinMaxInput("11", "12"));
    form.setUvalueOp("13");
    form.setUvalueDesign("14");
    return form;
  }

  private PadDesignOpConditions createEntity() {
    var entity = new PadDesignOpConditions();
    var form = createForm();

    entity.setTemperatureOpMinValue(form.getTemperatureOpMinMax().createMinOrNull());
    entity.setTemperatureOpMaxValue(form.getTemperatureOpMinMax().createMaxOrNull());
    entity.setTemperatureDesignMinValue(form.getTemperatureDesignMinMax().createMinOrNull());
    entity.setTemperatureDesignMaxValue(form.getTemperatureDesignMinMax().createMaxOrNull());
    entity.setPressureOpInternalValue(form.getPressureOpInternalExternal().createMinOrNull());
    entity.setPressureOpExternalValue(form.getPressureOpInternalExternal().createMaxOrNull());
    entity.setPressureDesignInternalValue(form.getPressureDesignInternalExternal().createMinOrNull());
    entity.setPressureDesignExternalValue(form.getPressureDesignInternalExternal().createMaxOrNull());
    entity.setFlowrateOpMinValue(form.getFlowrateOpMinMax().createMinOrNull());
    entity.setFlowrateOpMaxValue(form.getFlowrateOpMinMax().createMaxOrNull());
    entity.setFlowrateDesignMinValue(form.getFlowrateDesignMinMax().createMinOrNull());
    entity.setFlowrateDesignMaxValue(form.getFlowrateDesignMinMax().createMaxOrNull());
    entity.setUvalueOp(form.getUvalueOp() != null ? new BigDecimal(form.getUvalueOp()) : null);
    entity.setUvalueDesign(form.getUvalueOp() != null ? new BigDecimal(form.getUvalueDesign()) : null);

    return entity;
  }



  @Test
  public void mapEntityToForm_full() {
    var actualForm = new DesignOpConditionsForm();
    var entity = createEntity();
    designOpConditionsMappingService.mapEntityToForm(actualForm, entity);
    assertThat(actualForm).isEqualTo(createForm());
  }




  @Test
  public void mapFormToEntity_full() {
    var actualEntity = new PadDesignOpConditions();
    var form = createForm();
    designOpConditionsMappingService.mapFormToEntity(form, actualEntity);
    assertThat(actualEntity).isEqualTo(createEntity());
  }



}