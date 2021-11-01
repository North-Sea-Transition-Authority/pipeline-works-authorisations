package uk.co.ogauthority.pwa.features.application.tasks.crossings.types;

public class CrossingTypesView {

  private final Boolean pipelinesCrossed;
  private final Boolean cablesCrossed;
  private final Boolean medianLineCrossed;


  public CrossingTypesView(Boolean pipelinesCrossed, Boolean cablesCrossed, Boolean medianLineCrossed) {
    this.pipelinesCrossed = pipelinesCrossed;
    this.cablesCrossed = cablesCrossed;
    this.medianLineCrossed = medianLineCrossed;
  }


  public Boolean getPipelinesCrossed() {
    return pipelinesCrossed;
  }

  public Boolean getCablesCrossed() {
    return cablesCrossed;
  }

  public Boolean getMedianLineCrossed() {
    return medianLineCrossed;
  }
}
