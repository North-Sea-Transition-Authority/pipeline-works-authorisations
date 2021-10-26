package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

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