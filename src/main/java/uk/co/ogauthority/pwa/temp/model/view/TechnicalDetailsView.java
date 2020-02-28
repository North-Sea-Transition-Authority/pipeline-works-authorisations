package uk.co.ogauthority.pwa.temp.model.view;

import java.io.Serializable;
import java.util.List;
import uk.co.ogauthority.pwa.temp.model.view.pipelinetechnical.PipelineDesignCodeView;
import uk.co.ogauthority.pwa.temp.model.view.pipelinetechnical.PipelineFluidChemicalComponentView;

public class TechnicalDetailsView implements Serializable {

  private Integer designLifeSpanYears;
  private List<PipelineDesignCodeView> pipelineDesignCodeViewList;
  private String generalStatementOfPipelineDesign;
  private List<PipelineFluidChemicalComponentView> pipelineFluidChemicalComponentViewList;
  private String corrosionManagementStrategy;
  private boolean isTrenched;
  private String trenchingDescription;

  private String operatingTemperatureMin;
  private String operatingTemperatureMax;
  private String designTemperatureMin;
  private String designTemperatureMax;

  private String operatingPressureInternal;
  private String operatingPressureExternal;
  private String designPressureInternal;
  private String designPressureExternal;

  private String operatingFlowRateMin;
  private String operatingFlowRateMax;
  private String designFlowRateMin;
  private String designFlowRateMax;

  private String operatingUValue;
  private String designUValue;

  // temporary
  public static TechnicalDetailsView createExampleTechDetails() {
    TechnicalDetailsView v = new TechnicalDetailsView();
    v.setDesignLifeSpanYears(30);
    v.setCorrosionManagementStrategy("This is an example corrosion management strategy. " +
        "It contains many details and references to technical documents.");

    v.setGeneralStatementOfPipelineDesign("The Production and Gas Lift flowlines are flexible pipelines.\n" +
        "\n" +
        "The pipelines system has been designed in accordance with and meets the requirements of industry recognised codes and standards.");

    v.setPipelineDesignCodeViewList(
        List.of(
            new PipelineDesignCodeView(
                "Primary design code for pipeline/riser",
                "PD 8010 n2004 Part 2 Subsea PipelinesPD 8010 n2004 Part 2 Subsea Pipelines"
            ),
            new PipelineDesignCodeView(
                "Primary design code for piping on Manifolds",
                "PD 8010 n2004 Part 2 Subsea Pipelines, supplemented by other codes as appropriate."
            ),
            new PipelineDesignCodeView(
                "Manifolds",
                "PD8010 Part 2 supplemented by ANSI B31.2 and other ANSI codes where appropriate."
            ),
            new PipelineDesignCodeView(
                "Subsea Structures",
                "Designing and Constructing Fixed Offshore Platforms and supplemented by other codes as appropriate."
            )
        )
    );

    v.setPipelineFluidChemicalComponentViewList(
        List.of(
            new PipelineFluidChemicalComponentView("H2S", null, true),
            new PipelineFluidChemicalComponentView("CO2", String.valueOf(10)),
            new PipelineFluidChemicalComponentView("H2O", String.valueOf(0.124))
        )
    );

    v.setTrenched(true);
    v.setTrenchingDescription("The pipeline will be trenched by jetting and cutting seabed condition dependant and" +
        " simultaneously buried such that the top of the pipeline is a minimum of X.Xm below the natural seabed and" +
        " target trench depth of X.Xm.  The trench shall maintain a distance of XXm from any existing pipelines. ");
    return v;
  }

  public Integer getDesignLifeSpanYears() {
    return designLifeSpanYears;
  }

  public void setDesignLifeSpanYears(Integer designLifeSpanYears) {
    this.designLifeSpanYears = designLifeSpanYears;
  }

  public List<PipelineDesignCodeView> getPipelineDesignCodeViewList() {
    return pipelineDesignCodeViewList;
  }

  public void setPipelineDesignCodeViewList(
      List<PipelineDesignCodeView> pipelineDesignCodeViewList) {
    this.pipelineDesignCodeViewList = pipelineDesignCodeViewList;
  }

  public List<PipelineFluidChemicalComponentView> getPipelineFluidChemicalComponentViewList() {
    return pipelineFluidChemicalComponentViewList;
  }

  public void setPipelineFluidChemicalComponentViewList(
      List<PipelineFluidChemicalComponentView> pipelineFluidChemicalComponentViewList) {
    this.pipelineFluidChemicalComponentViewList = pipelineFluidChemicalComponentViewList;
  }

  public String getGeneralStatementOfPipelineDesign() {
    return generalStatementOfPipelineDesign;
  }

  public void setGeneralStatementOfPipelineDesign(String generalStatementOfPipelineDesign) {
    this.generalStatementOfPipelineDesign = generalStatementOfPipelineDesign;
  }

  public String getCorrosionManagementStrategy() {
    return corrosionManagementStrategy;
  }

  public void setCorrosionManagementStrategy(String corrosionManagementStrategy) {
    this.corrosionManagementStrategy = corrosionManagementStrategy;
  }

  public String getTrenchingDescription() {
    return trenchingDescription;
  }

  public void setTrenchingDescription(String trenchingDescription) {
    this.trenchingDescription = trenchingDescription;
  }

  public String getOperatingTemperatureMin() {
    return operatingTemperatureMin;
  }

  public void setOperatingTemperatureMin(String operatingTemperatureMin) {
    this.operatingTemperatureMin = operatingTemperatureMin;
  }

  public String getOperatingTemperatureMax() {
    return operatingTemperatureMax;
  }

  public void setOperatingTemperatureMax(String operatingTemperatureMax) {
    this.operatingTemperatureMax = operatingTemperatureMax;
  }

  public String getDesignTemperatureMin() {
    return designTemperatureMin;
  }

  public void setDesignTemperatureMin(String designTemperatureMin) {
    this.designTemperatureMin = designTemperatureMin;
  }

  public String getDesignTemperatureMax() {
    return designTemperatureMax;
  }

  public void setDesignTemperatureMax(String designTemperatureMax) {
    this.designTemperatureMax = designTemperatureMax;
  }

  public String getOperatingPressureInternal() {
    return operatingPressureInternal;
  }

  public void setOperatingPressureInternal(String operatingPressureInternal) {
    this.operatingPressureInternal = operatingPressureInternal;
  }

  public String getOperatingPressureExternal() {
    return operatingPressureExternal;
  }

  public void setOperatingPressureExternal(String operatingPressureExternal) {
    this.operatingPressureExternal = operatingPressureExternal;
  }

  public String getDesignPressureInternal() {
    return designPressureInternal;
  }

  public void setDesignPressureInternal(String designPressureInternal) {
    this.designPressureInternal = designPressureInternal;
  }

  public String getDesignPressureExternal() {
    return designPressureExternal;
  }

  public void setDesignPressureExternal(String designPressureExternal) {
    this.designPressureExternal = designPressureExternal;
  }

  public String getOperatingFlowRateMin() {
    return operatingFlowRateMin;
  }

  public void setOperatingFlowRateMin(String operatingFlowRateMin) {
    this.operatingFlowRateMin = operatingFlowRateMin;
  }

  public String getOperatingFlowRateMax() {
    return operatingFlowRateMax;
  }

  public void setOperatingFlowRateMax(String operatingFlowRateMax) {
    this.operatingFlowRateMax = operatingFlowRateMax;
  }

  public String getDesignFlowRateMin() {
    return designFlowRateMin;
  }

  public void setDesignFlowRateMin(String designFlowRateMin) {
    this.designFlowRateMin = designFlowRateMin;
  }

  public String getDesignFlowRateMax() {
    return designFlowRateMax;
  }

  public void setDesignFlowRateMax(String designFlowRateMax) {
    this.designFlowRateMax = designFlowRateMax;
  }

  public String getOperatingUValue() {
    return operatingUValue;
  }

  public void setOperatingUValue(String operatingUValue) {
    this.operatingUValue = operatingUValue;
  }

  public String getDesignUValue() {
    return designUValue;
  }

  public void setDesignUValue(String designUValue) {
    this.designUValue = designUValue;
  }

  public boolean getIsTrenched() {
    return isTrenched;
  }

  public void setTrenched(boolean trenched) {
    isTrenched = trenched;
  }
}
