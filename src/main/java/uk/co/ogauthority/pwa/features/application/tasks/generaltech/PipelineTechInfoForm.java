package uk.co.ogauthority.pwa.features.application.tasks.generaltech;


import java.util.Objects;

public class PipelineTechInfoForm  {

  private Integer estimatedAssetLife;
  private Boolean pipelineDesignedToStandards;
  private String pipelineStandardsDescription;
  private String corrosionDescription;
  private Boolean plannedPipelineTieInPoints;
  private String tieInPointsDescription;

  public PipelineTechInfoForm() {
  }

  public Integer getEstimatedAssetLife() {
    return estimatedAssetLife;
  }

  public void setEstimatedAssetLife(Integer estimatedAssetLife) {
    this.estimatedAssetLife = estimatedAssetLife;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineTechInfoForm that = (PipelineTechInfoForm) o;
    return Objects.equals(estimatedAssetLife, that.estimatedAssetLife)
        && Objects.equals(pipelineDesignedToStandards, that.pipelineDesignedToStandards)
        && Objects.equals(pipelineStandardsDescription, that.pipelineStandardsDescription)
        && Objects.equals(corrosionDescription, that.corrosionDescription)
        && Objects.equals(plannedPipelineTieInPoints, that.plannedPipelineTieInPoints)
        && Objects.equals(tieInPointsDescription, that.tieInPointsDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(estimatedAssetLife, pipelineDesignedToStandards, pipelineStandardsDescription,
        corrosionDescription, plannedPipelineTieInPoints, tieInPointsDescription);
  }
}
