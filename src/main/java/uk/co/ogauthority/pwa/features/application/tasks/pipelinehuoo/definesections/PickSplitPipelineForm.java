package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections;

/**
 * Form filled in when defining how many sections a pipeline with split HUOO roles has.
 */
public class PickSplitPipelineForm {

  private Integer pipelineId;

  private Integer numberOfSections;

  public Integer getPipelineId() {
    return pipelineId;
  }

  public void setPipelineId(Integer pipelineId) {
    this.pipelineId = pipelineId;
  }

  public Integer getNumberOfSections() {
    return numberOfSections;
  }

  public void setNumberOfSections(Integer numberOfSections) {
    this.numberOfSections = numberOfSections;
  }
}
