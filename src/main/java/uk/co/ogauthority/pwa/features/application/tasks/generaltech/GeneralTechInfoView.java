package uk.co.ogauthority.pwa.features.application.tasks.generaltech;

public class GeneralTechInfoView {


  private final Integer estimatedAssetLife;
  private final Boolean pipelineDesignedToStandards;
  private final String pipelineStandardsDescription;
  private final String corrosionDescription;
  private final Boolean plannedPipelineTieInPoints;
  private final String tieInPointsDescription;


  public GeneralTechInfoView(Integer estimatedAssetLife, Boolean pipelineDesignedToStandards,
                             String pipelineStandardsDescription, String corrosionDescription,
                             Boolean plannedPipelineTieInPoints, String tieInPointsDescription) {
    this.estimatedAssetLife = estimatedAssetLife;
    this.pipelineDesignedToStandards = pipelineDesignedToStandards;
    this.pipelineStandardsDescription = pipelineStandardsDescription;
    this.corrosionDescription = corrosionDescription;
    this.plannedPipelineTieInPoints = plannedPipelineTieInPoints;
    this.tieInPointsDescription = tieInPointsDescription;
  }


  public Integer getEstimatedAssetLife() {
    return estimatedAssetLife;
  }

  public Boolean getPipelineDesignedToStandards() {
    return pipelineDesignedToStandards;
  }

  public String getPipelineStandardsDescription() {
    return pipelineStandardsDescription;
  }

  public String getCorrosionDescription() {
    return corrosionDescription;
  }

  public Boolean getPlannedPipelineTieInPoints() {
    return plannedPipelineTieInPoints;
  }

  public String getTieInPointsDescription() {
    return tieInPointsDescription;
  }
}
