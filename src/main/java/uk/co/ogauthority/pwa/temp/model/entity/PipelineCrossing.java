package uk.co.ogauthority.pwa.temp.model.entity;

public class PipelineCrossing {

  private String pipelineNumber;
  private String ownerOfPipeline;

  public PipelineCrossing(String pipelineNumber, String ownerOfPipeline) {
    this.pipelineNumber = pipelineNumber;
    this.ownerOfPipeline = ownerOfPipeline;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public void setPipelineNumber(String pipelineNumber) {
    this.pipelineNumber = pipelineNumber;
  }

  public String getOwnerOfPipeline() {
    return ownerOfPipeline;
  }

  public void setOwnerOfPipeline(String ownerOfPipeline) {
    this.ownerOfPipeline = ownerOfPipeline;
  }
}
