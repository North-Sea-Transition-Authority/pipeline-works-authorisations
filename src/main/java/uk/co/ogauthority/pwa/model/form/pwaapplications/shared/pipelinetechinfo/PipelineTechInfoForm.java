package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo;


public class PipelineTechInfoForm  {

  private Integer estimatedFieldLife;
  private Boolean pipelineDesignedToStandards;
  private String pipelineStandardsDescription;
  private String corrosionDescription;
  private Boolean plannedPipelineTieInPoints;
  private String tieInPointsDescription;



  public Integer getEstimatedFieldLife() {
    return estimatedFieldLife;
  }

  public void setEstimatedFieldLife(Integer estimatedFieldLife) {
    this.estimatedFieldLife = estimatedFieldLife;
  }

  public Boolean getPipelineDesignedToStandards() {
    return pipelineDesignedToStandards;
  }

  public void setPipelineDesignedToStandards(Boolean pipelineDesignedToStandards) {
    this.pipelineDesignedToStandards = pipelineDesignedToStandards;
  }

  public String getPipelineStandardsDescription() {
    return pipelineStandardsDescription;
  }

  public void setPipelineStandardsDescription(String pipelineStandardsDescription) {
    this.pipelineStandardsDescription = pipelineStandardsDescription;
  }

  public String getCorrosionDescription() {
    return corrosionDescription;
  }

  public void setCorrosionDescription(String corrosionDescription) {
    this.corrosionDescription = corrosionDescription;
  }

  public Boolean getPlannedPipelineTieInPoints() {
    return plannedPipelineTieInPoints;
  }

  public void setPlannedPipelineTieInPoints(Boolean plannedPipelineTieInPoints) {
    this.plannedPipelineTieInPoints = plannedPipelineTieInPoints;
  }

  public String getTieInPointsDescription() {
    return tieInPointsDescription;
  }

  public void setTieInPointsDescription(String tieInPointsDescription) {
    this.tieInPointsDescription = tieInPointsDescription;
  }


}
