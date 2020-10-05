package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

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
