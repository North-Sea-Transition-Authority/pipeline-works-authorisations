package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;

/*
  A form builder class for PadPipelineOtherProperties that matches the data created by OtherPropertiesEntityBuilder used to aid testing.
 */
public class OtherPropertiesFormBuilder {


  public PipelineOtherPropertiesForm createFullForm() {
    var form = new PipelineOtherPropertiesForm();
    setPropertyFormMapData(form);
    setPhasesFormData(form);
    return form;
  }



  public void setPhasesFormData(PipelineOtherPropertiesForm form) {
    for (var phase: PropertyPhase.asList()) {
      form.getPhasesSelection().put(phase, "true");
    }
    form.setOtherPhaseDescription("my description");
  }


  private void setPropertyFormMapData (PipelineOtherPropertiesForm form) {
    form.addPropertyData(OtherPipelineProperty.WAX_CONTENT, createDataForm(PropertyAvailabilityOption.NOT_AVAILABLE));
    form.addPropertyData(OtherPipelineProperty.WAX_APPEARANCE_TEMPERATURE, createDataForm(PropertyAvailabilityOption.NOT_AVAILABLE));
    form.addPropertyData(OtherPipelineProperty.ACID_NUM, createDataForm(PropertyAvailabilityOption.NOT_AVAILABLE));
    form.addPropertyData(OtherPipelineProperty.VISCOSITY, createDataForm(PropertyAvailabilityOption.NOT_AVAILABLE));
    form.addPropertyData(OtherPipelineProperty.DENSITY_GRAVITY, createDataForm(PropertyAvailabilityOption.NOT_PRESENT));
    form.addPropertyData(OtherPipelineProperty.SULPHUR_CONTENT, createDataForm(PropertyAvailabilityOption.NOT_PRESENT));
    form.addPropertyData(OtherPipelineProperty.POUR_POINT, createDataForm(PropertyAvailabilityOption.NOT_PRESENT));
    form.addPropertyData(OtherPipelineProperty.SOLID_CONTENT, createDataForm(PropertyAvailabilityOption.NOT_PRESENT));
    form.addPropertyData(OtherPipelineProperty.MERCURY, createDataForm(PropertyAvailabilityOption.AVAILABLE,
        new MinMaxInput(String.valueOf(3), String.valueOf(5))));
    form.addPropertyData(OtherPipelineProperty.H20, createDataForm(PropertyAvailabilityOption.AVAILABLE,
        new MinMaxInput(String.valueOf(12), String.valueOf(15))));
  }


  private PipelineOtherPropertiesDataForm createDataForm
      (PropertyAvailabilityOption availabilityOption, MinMaxInput minMaxInput) {
    var dataForm = new PipelineOtherPropertiesDataForm();
    dataForm.setPropertyAvailabilityOption(availabilityOption);
    dataForm.setMinMaxInput(minMaxInput);
    return dataForm;
  }

  private PipelineOtherPropertiesDataForm createDataForm(PropertyAvailabilityOption availabilityOption) {
    var dataForm = new PipelineOtherPropertiesDataForm();
    dataForm.setPropertyAvailabilityOption(availabilityOption);
    return dataForm;
  }
















}