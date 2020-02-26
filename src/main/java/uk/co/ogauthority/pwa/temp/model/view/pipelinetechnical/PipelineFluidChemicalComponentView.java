package uk.co.ogauthority.pwa.temp.model.view.pipelinetechnical;

import java.io.Serializable;

public class PipelineFluidChemicalComponentView implements Serializable {

  private String componentName;
  private String molPercentage;
  private Boolean isTrace;

  public PipelineFluidChemicalComponentView(String componentName, String molPercentage) {
    this.componentName = componentName;
    this.molPercentage = molPercentage;
    this.isTrace = false;
  }

  public PipelineFluidChemicalComponentView(String componentName, String molPercentage, Boolean isTrace) {
    this.componentName = componentName;
    this.molPercentage = molPercentage;
    this.isTrace = isTrace;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public String getMolPercentage() {
    return molPercentage;
  }

  public void setMolPercentage(String molPercentage) {
    this.molPercentage = molPercentage;
  }

  public Boolean getIsTrace() {
    return isTrace;
  }

  public void setIsTrace(Boolean trace) {
    isTrace = trace;
  }
}
