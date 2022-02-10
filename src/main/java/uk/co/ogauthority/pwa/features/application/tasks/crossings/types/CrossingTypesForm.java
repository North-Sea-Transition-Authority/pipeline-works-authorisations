package uk.co.ogauthority.pwa.features.application.tasks.crossings.types;

public class CrossingTypesForm {

  private Boolean pipelinesCrossed;
  private Boolean cablesCrossed;
  private Boolean medianLineCrossed;

  public Boolean getPipelinesCrossed() {
    return pipelinesCrossed;
  }

  public void setPipelinesCrossed(Boolean pipelinesCrossed) {
    this.pipelinesCrossed = pipelinesCrossed;
  }

  public Boolean getCablesCrossed() {
    return cablesCrossed;
  }

  public void setCablesCrossed(Boolean cablesCrossed) {
    this.cablesCrossed = cablesCrossed;
  }

  public Boolean getMedianLineCrossed() {
    return medianLineCrossed;
  }

  public void setMedianLineCrossed(Boolean medianLineCrossed) {
    this.medianLineCrossed = medianLineCrossed;
  }
}
